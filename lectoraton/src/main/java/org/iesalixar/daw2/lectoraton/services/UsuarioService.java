package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.UsuarioCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioDTO;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioPerfilDTO;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.mappers.UsuarioMapper;
import org.iesalixar.daw2.lectoraton.entities.Role;
import org.iesalixar.daw2.lectoraton.repositories.RoleRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private static final String DEFAULT_ROL = "Lector";

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final UsuarioMapper usuarioMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final BibliotecaService bibliotecaService;
    private final EditorPromotionMailService editorPromotionMailService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RoleRepository roleRepository,
                          UsuarioMapper usuarioMapper,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                          BibliotecaService bibliotecaService,
                          EditorPromotionMailService editorPromotionMailService) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.bibliotecaService = bibliotecaService;
        this.editorPromotionMailService = editorPromotionMailService;
    }

    private static boolean tieneRolParaPublicar(Role r) {
        String n = r.getNombre();
        return n != null && "Editor".equalsIgnoreCase(n);
    }

    public Long getIdByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return usuario.getId();
    }

    public Optional<UsuarioDTO> getUsuarioById(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toDTO);
    }

    public Optional<UsuarioPerfilDTO> getUsuarioPerfil(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toPerfilDTO);
    }

    public List<UsuarioDTO> buscarUsuarios(Long usuarioActualId, String q) {
        String texto = q == null ? "" : q.trim();
        if (texto.length() < 2) {
            return List.of();
        }
        return usuarioRepository.buscarPorTexto(texto).stream()
                .filter(u -> !u.getId().equals(usuarioActualId))
                .limit(15)
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Set<Long> getSeguidosIds(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return usuario.getSeguidos().stream().map(Usuario::getId).collect(Collectors.toSet());
    }

    public List<UsuarioDTO> getSeguidos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return usuario.getSeguidos().stream()
                .map(usuarioMapper::toDTO)
                .sorted((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()))
                .collect(Collectors.toList());
    }

    public void seguirUsuario(Long seguidorId, Long seguidoId) {
        if (seguidorId.equals(seguidoId)) {
            throw new IllegalArgumentException("No puedes seguirte a ti mismo.");
        }
        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario a seguir no encontrado."));
        seguidor.getSeguidos().add(seguido);
        usuarioRepository.save(seguidor);
    }

    public void dejarDeSeguirUsuario(Long seguidorId, Long seguidoId) {
        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario a dejar de seguir no encontrado."));
        seguidor.getSeguidos().remove(seguido);
        usuarioRepository.save(seguidor);
    }

    public UsuarioDTO create(UsuarioCreateDTO dto) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }
        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        Set<Role> roles = new HashSet<>();
        roleRepository.findByNombre(DEFAULT_ROL).ifPresent(roles::add);
        if (roles.isEmpty()) {
            throw new IllegalStateException("No existe el rol por defecto para nuevos usuarios.");
        }
        usuario.setRoles(roles);
        Usuario saved = usuarioRepository.save(usuario);
        bibliotecaService.crearBibliotecasFijasSiFaltan(saved);
        return usuarioMapper.toDTO(saved);
    }

    /**
     * Localiza usuario por email o lo crea (rol Lector, bibliotecas fijas).
     * Misma cuenta si el correo coincide con uno ya registrado (login local u otro OAuth).
     */
    @Transactional
    public Usuario findOrProvisionFromGoogleOAuth(String emailRaw,
                                                  String nombreGiven,
                                                  String apellidosFamily,
                                                  String pictureUrlNullable) {
        String email = emailRaw.trim().toLowerCase(Locale.ROOT);
        Optional<Usuario> existing = usuarioRepository.findByEmail(email);
        if (existing.isPresent()) {
            Usuario u = existing.get();
            String prevIcono = u.getIcono();
            maybeSetIcon(u, pictureUrlNullable);
            return Objects.equals(prevIcono, u.getIcono()) ? u : usuarioRepository.save(u);
        }

        String ng = nombreGiven == null ? "" : nombreGiven.trim();
        String af = apellidosFamily == null ? "" : apellidosFamily.trim();
        String nombreFinal = truncate(ng, 50);
        String apellidosFinal = truncate(af, 100);

        Usuario usuario = new Usuario();
        usuario.setUsername(uniquifyUsernameFromEmail(email));
        usuario.setPassword(passwordEncoder.encode("OAUTH:" + UUID.randomUUID()));
        usuario.setNombre(nombreFinal.isBlank() ? "Usuario" : nombreFinal);
        usuario.setApellidos(apellidosFinal.isBlank() ? "-" : apellidosFinal);
        usuario.setEmail(email);

        maybeSetIcon(usuario, pictureUrlNullable);

        Set<Role> roles = new HashSet<>();
        roleRepository.findByNombre(DEFAULT_ROL).ifPresent(roles::add);
        if (roles.isEmpty()) {
            throw new IllegalStateException("No existe el rol por defecto para nuevos usuarios.");
        }
        usuario.setRoles(roles);

        Usuario saved = usuarioRepository.save(usuario);
        bibliotecaService.crearBibliotecasFijasSiFaltan(saved);
        logger.info("Usuario provisioning OAuth Google: id {} username {}", saved.getId(), saved.getUsername());
        return saved;
    }

    private void maybeSetIcon(Usuario usuario, String pictureUrlNullable) {
        if (!StringUtils.hasText(pictureUrlNullable)) {
            return;
        }
        if (usuario.getIcono() != null && !usuario.getIcono().isBlank()) {
            return;
        }
        String t = pictureUrlNullable.trim();
        usuario.setIcono(t.length() > 255 ? t.substring(0, 255) : t);
    }

    private static String truncate(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max);
    }

    private String uniquifyUsernameFromEmail(String emailNormalized) {
        int at = emailNormalized.indexOf('@');
        String local = at > 0 ? emailNormalized.substring(0, at) : emailNormalized;
        String base = local.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_").replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        if (base.isBlank()) {
            base = "lector";
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }

        String candidate = base.length() <= 50 ? base : base.substring(0, 50);
        if (!usuarioRepository.existsByUsername(candidate)) {
            return candidate;
        }

        int attempts = 0;
        while (attempts++ < 30) {
            String suffix = "_" + ThreadLocalRandom.current().nextInt(100000, 999999);
            candidate = truncate(base + suffix, 50);
            if (!usuarioRepository.existsByUsername(candidate)) {
                return candidate;
            }
        }
        candidate = truncate(base + "_" + UUID.randomUUID().toString().replace("-", ""), 50);
        if (!usuarioRepository.existsByUsername(candidate)) {
            return candidate;
        }
        throw new IllegalStateException("No se pudo generar un nombre de usuario único para OAuth.");
    }

    public UsuarioPerfilDTO updateBio(Long usuarioId, String bio) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuario.setBio(bio == null ? null : bio.trim());
        return usuarioMapper.toPerfilDTO(usuarioRepository.save(usuario));
    }

    /**
     * Envía correo al administrador para solicitar el rol Editor (añadir libros).
     */
    public void solicitarRolEditor(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        boolean ya = usuario.getRoles().stream().anyMatch(UsuarioService::tieneRolParaPublicar);
        if (ya) {
            throw new IllegalArgumentException("Ya tienes permiso para añadir libros.");
        }
        editorPromotionMailService.enviarSolicitud(usuario);
    }

    /**
     * Otorga rol Editor (enlace firmado del correo).
     */
    public void otorgarRolEditor(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        boolean ya = usuario.getRoles().stream().anyMatch(UsuarioService::tieneRolParaPublicar);
        if (ya) {
            throw new IllegalArgumentException("Este usuario ya tiene permiso para añadir libros.");
        }
        Role editor = roleRepository.findByNombre("Editor")
                .orElseThrow(() -> new IllegalStateException("No existe el rol «Editor» en la base de datos."));
        usuario.getRoles().add(editor);
        usuarioRepository.save(usuario);
        logger.info("Rol Editor asignado manualmente a usuario id {}", usuarioId);
        editorPromotionMailService.enviarAprobacionEditor(usuario);
    }
}
