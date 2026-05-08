package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.LibroDetalleDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroMiniDTO;
import org.iesalixar.daw2.lectoraton.dtos.ResenaResumenDTO;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioMiniDTO;
import org.iesalixar.daw2.lectoraton.entities.EstadoLectura;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibro;
import org.iesalixar.daw2.lectoraton.repositories.BibliotecaLibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.LibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.ResenaRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioLibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LibroDetalleService {

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioLibroRepository usuarioLibroRepository;
    private final ResenaRepository resenaRepository;
    private final BibliotecaLibroRepository bibliotecaLibroRepository;

    public LibroDetalleService(LibroRepository libroRepository,
                               UsuarioRepository usuarioRepository,
                               UsuarioLibroRepository usuarioLibroRepository,
                               ResenaRepository resenaRepository,
                               BibliotecaLibroRepository bibliotecaLibroRepository) {
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioLibroRepository = usuarioLibroRepository;
        this.resenaRepository = resenaRepository;
        this.bibliotecaLibroRepository = bibliotecaLibroRepository;
    }
    /**
     * Obtiene el detalle de un libro.
     * @param libroId ID del libro.
     * @param usuarioId ID del usuario.
     * @return LibroDetalleDTO.
     */
    public LibroDetalleDTO getDetalle(Long libroId, Long usuarioId) {   
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));

        LibroDetalleDTO dto = new LibroDetalleDTO();
        dto.setId(libro.getId());
        dto.setTitulo(libro.getTitulo());
        dto.setSagaNombre(libro.getSagaNombre());
        dto.setNumeroSaga(libro.getNumeroSaga());
        dto.setAutorNombre(libro.getAutor() != null ? libro.getAutor().getNombreCompleto() : null);
        dto.setSinopsis(libro.getSinopsis());
        dto.setPortada(libro.getPortada());
        dto.setGeneros(libro.getGeneros().stream().map(g -> g.getNombre()).sorted().collect(Collectors.toList()));
        dto.setTropos(libro.getTropos().stream().map(t -> t.getNombre()).sorted().collect(Collectors.toList()));
        dto.setPaginas(libro.getNumPaginas());
        dto.setFechaPublicacion(libro.getFechaPublicacion());
        dto.setYaEnAlgunaBiblioteca(bibliotecaLibroRepository.existsByBibliotecaUsuarioIdAndLibroId(usuarioId, libroId));
        dto.setMiProgreso(usuarioLibroRepository.findByUsuarioIdAndLibroId(usuarioId, libroId)
                .map(ul -> UsuarioLibroProgresoService.toDto(ul, libro))
                .orElseGet(() -> UsuarioLibroProgresoService.progresoPorDefecto(libro)));
        dto.setResumenResenas(buildResumen(libroId));
        dto.setAmigosQueHanLeido(buildAmigosQueHanLeido(libroId, usuarioId));
        dto.setOtrosMismoAutor(buildOtrosAutor(libro));
        dto.setOtrosParecidos(buildSimilares(libro));
        return dto;
    }

    /**
     * Construye el resumen de reseñas.
     * @param libroId ID del libro.
     * @return ResenaResumenDTO.
     */
    private ResenaResumenDTO buildResumen(Long libroId) {
        ResenaResumenDTO resumen = new ResenaResumenDTO();
        Double media = usuarioLibroRepository.averageCalificacionByLibroId(libroId);
        long totalCalificaciones = usuarioLibroRepository.countByLibroIdAndCalificacionIsNotNull(libroId);
        long totalResenas = resenaRepository.countByLibroId(libroId);

        Map<Integer, Long> dist = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            dist.put(i, 0L);
        }
        List<UsuarioLibro> valoraciones = usuarioLibroRepository.findAll().stream()
                .filter(ul -> ul.getLibro() != null && libroId.equals(ul.getLibro().getId()))
                .filter(ul -> ul.getCalificacion() != null)
                .toList();
        for (UsuarioLibro ul : valoraciones) {
            dist.computeIfPresent(ul.getCalificacion(), (k, v) -> v + 1);
        }

        resumen.setMediaCalificaciones(media == null ? 0.0 : media);
        resumen.setTotalCalificaciones(totalCalificaciones);
        resumen.setTotalResenas(totalResenas);
        resumen.setDistribucionEstrellas(dist);
        return resumen;
    }

    /**
     * Construye la lista de amigos que han leido el libro.
     * @param libroId ID del libro.
     * @param usuarioId ID del usuario.
     * @return Lista de UsuarioMiniDTO.
     */
    private List<UsuarioMiniDTO> buildAmigosQueHanLeido(Long libroId, Long usuarioId) {
        Usuario actual = usuarioRepository.findById(usuarioId).orElse(null);
        if (actual == null || actual.getSeguidos().isEmpty()) {
            return List.of();
        }
        List<Long> seguidosIds = actual.getSeguidos().stream().map(Usuario::getId).toList();
        return usuarioLibroRepository.findByLibroIdAndUsuarioIdInAndEstado(libroId, seguidosIds, EstadoLectura.leido)
                .stream()
                .map(UsuarioLibro::getUsuario)
                .filter(u -> u != null)
                .distinct()
                .map(this::toUsuarioMini)
                .limit(8)
                .collect(Collectors.toList());
    }

    /**
     * Construye la lista de otros libros del mismo autor.
     * @param libro Libro.
     * @return Lista de LibroMiniDTO.
     */
    private List<LibroMiniDTO> buildOtrosAutor(Libro libro) {
        if (libro.getAutor() == null) {
            return List.of();
        }
        return libroRepository.findByAutorId(libro.getAutor().getId(), PageRequest.of(0, 8)).getContent().stream()
                .filter(l -> !l.getId().equals(libro.getId()))
                .map(this::toLibroMini)
                .collect(Collectors.toList());
    }

    /**
     * Construye la lista de libros similares.
     * @param libro Libro.
     * @return Lista de LibroMiniDTO.
     */
    private List<LibroMiniDTO> buildSimilares(Libro libro) {
        List<Long> generoIds = libro.getGeneros().stream().map(g -> g.getId()).toList();
        List<Long> tropoIds = libro.getTropos().stream().map(t -> t.getId()).toList();
        if (generoIds.isEmpty() && tropoIds.isEmpty()) {
            return List.of();
        }
        List<Libro> similares = new ArrayList<>();
        if (!generoIds.isEmpty() && !tropoIds.isEmpty()) {
            similares = libroRepository.findSimilares(libro.getId(), generoIds, tropoIds, PageRequest.of(0, 10));
        } else if (!generoIds.isEmpty()) {
            similares = libroRepository.findAll(PageRequest.of(0, 30)).getContent().stream()
                    .filter(l -> !l.getId().equals(libro.getId()))
                    .filter(l -> l.getGeneros().stream().anyMatch(g -> generoIds.contains(g.getId())))
                    .collect(Collectors.toList());
        } else {
            similares = libroRepository.findAll(PageRequest.of(0, 30)).getContent().stream()
                    .filter(l -> !l.getId().equals(libro.getId()))
                    .filter(l -> l.getTropos().stream().anyMatch(t -> tropoIds.contains(t.getId())))
                    .collect(Collectors.toList());
        }
        return similares.stream()
                .distinct()
                .sorted(Comparator.comparing(Libro::getTitulo, String.CASE_INSENSITIVE_ORDER))
                .limit(8)
                .map(this::toLibroMini)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un libro a un mini libro.
     * @param libro Libro.
     * @return LibroMiniDTO.
     */
    private LibroMiniDTO toLibroMini(Libro libro) {
        LibroMiniDTO dto = new LibroMiniDTO();
        dto.setId(libro.getId());
        dto.setTitulo(libro.getTitulo());
        dto.setPortada(libro.getPortada());
        dto.setAutorNombre(libro.getAutor() != null ? libro.getAutor().getNombreCompleto() : null);
        return dto;
    }

    /**
     * Convierte un usuario a un mini usuario.
     * @param usuario Usuario.
     * @return UsuarioMiniDTO.
     */
    private UsuarioMiniDTO toUsuarioMini(Usuario usuario) {
        UsuarioMiniDTO dto = new UsuarioMiniDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombreCompleto(usuario.getNombre() + " " + usuario.getApellidos());
        dto.setIcono(usuario.getIcono());
        return dto;
    }
}
