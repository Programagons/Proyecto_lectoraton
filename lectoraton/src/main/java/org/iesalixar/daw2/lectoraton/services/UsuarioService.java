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
import org.springframework.web.multipart.MultipartFile;

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
    private final FileStorageService fileStorageService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RoleRepository roleRepository,
                          UsuarioMapper usuarioMapper,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                          BibliotecaService bibliotecaService,
                          EditorPromotionMailService editorPromotionMailService,
                          FileStorageService fileStorageService) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.bibliotecaService = bibliotecaService;
        this.editorPromotionMailService = editorPromotionMailService;
        this.fileStorageService = fileStorageService;
    }


    /**
     * Verifica si un rol tiene permiso para publicar libros.
     * @param r El rol a verificar.
     * @return true si el rol tiene permiso para publicar libros, false en caso contrario.
     */
    private static boolean tieneRolParaPublicar(Role r) {
        String n = r.getNombre();
        return n != null && "Editor".equalsIgnoreCase(n);
    }

    /**
     * Obtiene el ID de un usuario por su nombre de usuario.
     * @param username El nombre de usuario.
     * @return El ID del usuario.
     */
    public Long getIdByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        return usuario.getId();
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario.
     * @return El usuario.
     */
    public Optional<UsuarioDTO> getUsuarioById(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toDTO);
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario.
     * @return El usuario.
     */
    public Optional<UsuarioPerfilDTO> getUsuarioPerfil(Long id) {
        return usuarioRepository.findById(id).map(usuarioMapper::toPerfilDTO);
    }

    /**
     * Busca usuarios por texto.
     * @param usuarioActualId El ID del usuario actual.
     * @param q El texto a buscar.
     * @return La lista de usuarios encontrados.
     */
    public List<UsuarioDTO> buscarUsuarios(Long usuarioActualId, String q) {
        String texto = q == null ? "" : q.trim();
        // Si el texto es menor a 2 caracteres, se devuelve una lista vacía.
        if (texto.length() < 2) {
            return List.of();
        }
        // Se buscan los usuarios por texto.
        return usuarioRepository.buscarPorTexto(texto).stream()
                .filter(u -> !u.getId().equals(usuarioActualId))
                // Se limitan a 15 usuarios.
                .limit(15)
                // Se convierten a DTO.
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los IDs de los usuarios seguidos por un usuario.
     * @param usuarioId El ID del usuario.
     * @return Los IDs de los usuarios seguidos.
     */
    public Set<Long> getSeguidosIds(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return usuario.getSeguidos().stream().map(Usuario::getId).collect(Collectors.toSet());
    }

    /**
     * Obtiene los usuarios seguidos por un usuario.
     * @param usuarioId El ID del usuario.
     * @return Los usuarios seguidos.
     */
    public List<UsuarioDTO> getSeguidos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return usuario.getSeguidos().stream()
                // Se convierten a DTO.
                .map(usuarioMapper::toDTO)
                // Se ordenan por nombre de usuario.
                .sorted((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Sigue a un usuario.
     * @param seguidorId El ID del seguidor.
     * @param seguidoId El ID del seguido.
     */
    public void seguirUsuario(Long seguidorId, Long seguidoId) {
        // Si el seguidor es el mismo que el seguido, se lanza una excepción.
        if (seguidorId.equals(seguidoId)) {
            throw new IllegalArgumentException("No puedes seguirte a ti mismo.");
        }
        // Se obtiene el seguidor.
        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        // Se obtiene el seguido.
        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario a seguir no encontrado."));
        // Se agrega el seguido al seguidor.
        seguidor.getSeguidos().add(seguido);
        // Se guarda el seguidor.
        usuarioRepository.save(seguidor);
    }

    /**
     * Deja de seguir a un usuario.
     * @param seguidorId El ID del seguidor.
     * @param seguidoId El ID del seguido.
     */
    public void dejarDeSeguirUsuario(Long seguidorId, Long seguidoId) {
        // Se obtiene el seguidor.
        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        // Se obtiene el seguido.
        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario a dejar de seguir no encontrado."));
        // Se elimina el seguido del seguidor.
        seguidor.getSeguidos().remove(seguido);
        // Se guarda el seguidor.
        usuarioRepository.save(seguidor);
    }

    /**
     * Crea un nuevo usuario.
     * @param dto El DTO del usuario.
     * @return El usuario creado.
     */
    public UsuarioDTO create(UsuarioCreateDTO dto) {
        // Si el nombre de usuario ya existe, se lanza una excepción.
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        // Si el email ya está registrado, se lanza una excepción.
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }
        // Se crea el usuario.
        Usuario usuario = usuarioMapper.toEntity(dto);
        // Se encripta la contraseña.
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        // Se crea el conjunto de roles.
        Set<Role> roles = new HashSet<>();
        // Se busca el rol por defecto.
        roleRepository.findByNombre(DEFAULT_ROL).ifPresent(roles::add);
        if (roles.isEmpty()) {
            throw new IllegalStateException("No existe el rol por defecto para nuevos usuarios.");
        }
        usuario.setRoles(roles);
        // Se guarda el usuario.
        Usuario saved = usuarioRepository.save(usuario);
        // Se crean las bibliotecas fijas si faltan.
        bibliotecaService.crearBibliotecasFijasSiFaltan(saved);
        return usuarioMapper.toDTO(saved);
    }

    /**
     * Localiza usuario por email o lo crea (rol Lector, bibliotecas fijas).
     * Misma cuenta si el correo coincide con uno ya registrado (login local u otro OAuth).
     * @param emailRaw El email del usuario.
     * @param nombreGiven El nombre del usuario.
     * @param apellidosFamily Los apellidos del usuario.
     * @param pictureUrlNullable La URL de la imagen del usuario.
     * @return El usuario encontrado o creado.
     */
    @Transactional
    public Usuario findOrProvisionFromGoogleOAuth(String emailRaw,
                                                  String nombreGiven,
                                                  String apellidosFamily,
                                                  String pictureUrlNullable) {
        String email = emailRaw.trim().toLowerCase(Locale.ROOT);
        // Se busca el usuario por email.
        Optional<Usuario> existing = usuarioRepository.findByEmail(email);
        // Si el usuario existe, se actualiza.
        if (existing.isPresent()) {
            Usuario u = existing.get();
            // Se actualiza el icono del usuario.
            String prevIcono = u.getIcono();
            // Se actualiza el icono del usuario.
            maybeSetIcon(u, pictureUrlNullable);
            // Si el icono no ha cambiado, se devuelve el usuario.
            return Objects.equals(prevIcono, u.getIcono()) ? u : usuarioRepository.save(u);
        }

        // Se crea el nombre de usuario.
        String ng = nombreGiven == null ? "" : nombreGiven.trim();
        // Se crea el apellido del usuario.
        String af = apellidosFamily == null ? "" : apellidosFamily.trim();
        // Se crea el nombre final del usuario.
        String nombreFinal = truncate(ng, 50);
        // Se crea el apellido final del usuario.
        String apellidosFinal = truncate(af, 100);

        // Se crea el usuario.
        Usuario usuario = new Usuario();
        // Se crea el nombre de usuario.
        usuario.setUsername(uniquifyUsernameFromEmail(email));
        // Se crea la contraseña del usuario.
        usuario.setPassword(passwordEncoder.encode("OAUTH:" + UUID.randomUUID()));
        // Se crea el nombre del usuario.
        usuario.setNombre(nombreFinal.isBlank() ? "Usuario" : nombreFinal);
        usuario.setApellidos(apellidosFinal.isBlank() ? "-" : apellidosFinal);
        // Se crea el email del usuario.
        usuario.setEmail(email);

        // Se actualiza el icono del usuario.
        maybeSetIcon(usuario, pictureUrlNullable);
        // Se crea el conjunto de roles.

        Set<Role> roles = new HashSet<>();
        // Se busca el rol por defecto.
        roleRepository.findByNombre(DEFAULT_ROL).ifPresent(roles::add);
        // Si el rol no existe, se lanza una excepción.
        if (roles.isEmpty()) {
            throw new IllegalStateException("No existe el rol por defecto para nuevos usuarios.");
        }
        // Se asigna el conjunto de roles al usuario.
        usuario.setRoles(roles);
        // Se guarda el usuario.

        Usuario saved = usuarioRepository.save(usuario);
        // Se crean las bibliotecas fijas si faltan.
        bibliotecaService.crearBibliotecasFijasSiFaltan(saved);
        // Se loggea el usuario.
        logger.info("Usuario provisioning OAuth Google: id {} username {}", saved.getId(), saved.getUsername());
        return saved;
    }

    /**
     * Actualiza el icono del usuario.
     * @param usuario El usuario.
     * @param pictureUrlNullable La URL de la imagen del usuario.
     */
    private void maybeSetIcon(Usuario usuario, String pictureUrlNullable) {
        // Si la URL de la imagen es null o vacía, se devuelve.
        if (!StringUtils.hasText(pictureUrlNullable)) {
            return;
        }
        // Si el icono del usuario no es null o vacío, se devuelve.
        if (usuario.getIcono() != null && !usuario.getIcono().isBlank()) {
            return;
        }
        // Se crea la URL de la imagen del usuario.
        String t = pictureUrlNullable.trim();
        // Se asigna la URL de la imagen del usuario al usuario.
        usuario.setIcono(t.length() > 255 ? t.substring(0, 255) : t);
    }

    /**
     * Trunca un texto.
     * @param text El texto a truncar.
     * @param max El máximo de caracteres.
     * @return El texto truncado.
     */
    private static String truncate(String text, int max) {
        // Si el texto es menor o igual al máximo, se devuelve.
        if (text.length() <= max) {
            return text;
        }
        // Se devuelve el texto truncado.
        return text.substring(0, max);
    }

    /**
     * Genera un nombre de usuario único a partir de un email.
     * @param emailNormalized El email normalizado.
     * @return El nombre de usuario único.
     */
    private String uniquifyUsernameFromEmail(String emailNormalized) {
        // Se obtiene el índice de la @.
        int at = emailNormalized.indexOf('@');
        // Se obtiene el local del email.
        String local = at > 0 ? emailNormalized.substring(0, at) : emailNormalized;
        // Se crea la base del nombre de usuario.
        String base = local.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_").replaceAll("_+", "_").replaceAll("^_+|_+$", "");
        // Si la base es vacía, se asigna "lector".
        if (base.isBlank()) {
            base = "lector";
        }
        // Si la base es mayor a 40 caracteres, se trunca.
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }

        // Se crea el candidato del nombre de usuario.
        String candidate = base.length() <= 50 ? base : base.substring(0, 50);
        // Si el candidato no existe, se devuelve.
        if (!usuarioRepository.existsByUsername(candidate)) {
            return candidate;
        }

        // Se crea el intento del nombre de usuario.
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

    /**
     * Actualiza la biografía del usuario.
     * @param usuarioId El ID del usuario.
     * @param bio La biografía del usuario.
     * @return El usuario actualizado.
     */
    public UsuarioPerfilDTO updateBio(Long usuarioId, String bio) {
        // Se obtiene el usuario.
        Usuario usuario = usuarioRepository.findById(usuarioId)
                // Si el usuario no existe, se lanza una excepción.
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuario.setBio(bio == null ? null : bio.trim());
        // Se guarda el usuario.
        return usuarioMapper.toPerfilDTO(usuarioRepository.save(usuario));
    }

    /**
     * Actualiza el icono del usuario.
     * @param usuarioId El ID del usuario.
     * @param iconoFile El archivo del icono.
     * @return El usuario actualizado.
     */
    public UsuarioPerfilDTO updateIcono(Long usuarioId, MultipartFile iconoFile) {
        // Se obtiene el usuario.
        Usuario usuario = usuarioRepository.findById(usuarioId)
                // Si el usuario no existe, se lanza una excepción.
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        // Si el archivo del icono es null o vacío, se lanza una excepción.
        if (iconoFile == null || iconoFile.isEmpty()) {
            // Se lanza una excepción.
            throw new IllegalArgumentException("Selecciona una imagen.");
        }
        // Se obtiene el icono antiguo del usuario.
        String oldIcono = usuario.getIcono();
        // Se guarda el archivo del icono.
        String fileName = fileStorageService.saveFile(iconoFile);
        if (fileName == null || fileName.isBlank()) {
            throw new RuntimeException("No se pudo guardar la imagen de perfil.");
        }
        usuario.setIcono("/uploads/" + fileName);
        Usuario saved = usuarioRepository.save(usuario);
        if (oldIcono != null && !oldIcono.isBlank() && oldIcono.startsWith("/uploads/")) {
            fileStorageService.deleteFile(oldIcono.replace("/uploads/", ""));
        }
        return usuarioMapper.toPerfilDTO(saved);
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
