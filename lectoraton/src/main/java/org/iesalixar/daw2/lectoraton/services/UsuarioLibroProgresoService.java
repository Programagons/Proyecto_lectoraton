package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.ProgresoLecturaDTO;
import org.iesalixar.daw2.lectoraton.dtos.ProgresoLecturaUpdateDTO;
import org.iesalixar.daw2.lectoraton.dtos.UltimoProgresoLibroDTO;
import org.iesalixar.daw2.lectoraton.entities.EstadoLectura;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibro;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibroId;
import org.iesalixar.daw2.lectoraton.repositories.LibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioLibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioLibroProgresoService {

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioLibroRepository usuarioLibroRepository;
    private final ActividadUsuarioService actividadUsuarioService;

    public UsuarioLibroProgresoService(LibroRepository libroRepository,
                                       UsuarioRepository usuarioRepository,
                                       UsuarioLibroRepository usuarioLibroRepository,
                                       ActividadUsuarioService actividadUsuarioService) {
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioLibroRepository = usuarioLibroRepository;
        this.actividadUsuarioService = actividadUsuarioService;
    }

    /**
     * Actualiza el progreso de lectura de un libro.
     * @param libroId ID del libro.
     * @param usuarioId ID del usuario.
     * @param body DTO con los datos para actualizar el progreso de lectura.
     * @return ProgresoLecturaDTO.
     */
    @Transactional 
    public ProgresoLecturaDTO actualizar(Long libroId, Long usuarioId, ProgresoLecturaUpdateDTO body) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        Integer numPaginas = libro.getNumPaginas();
        int pagina = resolverPagina(body, numPaginas);
        if (numPaginas != null && numPaginas > 0 && pagina > numPaginas) {
            throw new IllegalArgumentException("La página actual no puede superar el total de páginas del libro (" + numPaginas + ").");
        }

        EstadoLectura estado = resolverEstado(pagina, numPaginas, body.getEstado());

        UsuarioLibro ul = usuarioLibroRepository.findByUsuarioIdAndLibroId(usuarioId, libroId)
                .orElseGet(() -> crearVacio(usuarioId, libro));

        ul.setPaginaActual(pagina);
        ul.setEstado(estado);
        ul.setFechaActualizacion(LocalDateTime.now());

        usuarioLibroRepository.save(ul);
        ProgresoLecturaDTO dto = toDto(ul, libro);
        Usuario actor = usuarioRepository.getReferenceById(usuarioId);
        actividadUsuarioService.registrarProgresoLectura(actor, libro, dto);
        return dto;
    }

    private int resolverPagina(ProgresoLecturaUpdateDTO body, Integer numPaginas) {
        if (body.getPaginaActual() != null) {
            return body.getPaginaActual();
        }
        Double porcentaje = body.getPorcentajeActual();
        if (porcentaje == null) {
            throw new IllegalArgumentException("Debes indicar paginaActual o porcentajeActual.");
        }
        if (numPaginas == null || numPaginas <= 0) {
            throw new IllegalArgumentException("Este libro no tiene total de páginas para calcular porcentaje.");
        }
        double ratio = Math.min(100.0, Math.max(0.0, porcentaje)) / 100.0;
        return (int) Math.round(ratio * numPaginas);
    }

    /**
     * Convierte un usuario libro a un DTO de progreso de lectura.
     * @param ul Usuario libro.
     * @param libro Libro.
     * @return ProgresoLecturaDTO.
     */
    public static ProgresoLecturaDTO toDto(UsuarioLibro ul, Libro libro) {
        ProgresoLecturaDTO dto = new ProgresoLecturaDTO();
        int pag = ul.getPaginaActual() != null ? ul.getPaginaActual() : 0;
        dto.setPaginaActual(pag);
        dto.setPaginasTotales(libro.getNumPaginas());
        dto.setPorcentaje(calcularPorcentaje(pag, libro.getNumPaginas()));
        dto.setEstado(ul.getEstado() != null ? ul.getEstado().name() : EstadoLectura.quiero_leer.name());
        dto.setFechaActualizacion(ul.getFechaActualizacion());
        return dto;
    }

    /** Valores por defecto cuando aún no hay fila en usuarios_libros */
    public static ProgresoLecturaDTO progresoPorDefecto(Libro libro) {
        ProgresoLecturaDTO dto = new ProgresoLecturaDTO();
        dto.setPaginaActual(0);
        dto.setPaginasTotales(libro.getNumPaginas());
        dto.setPorcentaje(0.0);
        dto.setEstado(EstadoLectura.quiero_leer.name());
        dto.setFechaActualizacion(null);
        return dto;
    }

    /**
     * Calcula el porcentaje de progreso de lectura.
     * @param paginaActual Página actual.
     * @param numPaginas Total de páginas.
     * @return Porcentaje de progreso de lectura.
     */
    private static double calcularPorcentaje(int paginaActual, Integer numPaginas) {
        if (numPaginas == null || numPaginas <= 0) {
            return 0.0;
        }
        double raw = (paginaActual * 100.0) / numPaginas;
        return Math.round(raw * 10.0) / 10.0;
    }

    /**
     * Resuelve el estado de lectura.
     * @param paginaActual Página actual.
     * @param numPaginas Total de páginas.
     * @param estadoRaw Estado de lectura.
     * @return Estado de lectura.
     */
    private EstadoLectura resolverEstado(int paginaActual, Integer numPaginas, String estadoRaw) {
        String s = estadoRaw == null ? "" : estadoRaw.trim();
        if (!s.isEmpty()) {
            try {
                return EstadoLectura.valueOf(s);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Estado no válido. Use: quiero_leer, leyendo, leido, abandonado.");
            }
        }
        if (numPaginas != null && numPaginas > 0 && paginaActual >= numPaginas) {
            return EstadoLectura.leido;
        }
        if (paginaActual > 0) {
            return EstadoLectura.leyendo;
        }
        return EstadoLectura.quiero_leer;
    }

    /**
     * Crea un usuario libro vacío.
     * @param usuarioId ID del usuario.
     * @param libro Libro.
     * @return Usuario libro.
     */
    private UsuarioLibro crearVacio(Long usuarioId, Libro libro) {
        UsuarioLibro nuevo = new UsuarioLibro();
        nuevo.setId(new UsuarioLibroId(usuarioId, libro.getId()));
        nuevo.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        nuevo.setLibro(libroRepository.getReferenceById(libro.getId()));
        nuevo.setPaginaActual(0);
        nuevo.setEstado(EstadoLectura.quiero_leer);
        return nuevo;
    }

    /**
     * Último libro en el que el usuario guardó progreso ({@code fecha_actualizacion} no nula).
     * @param usuarioId ID del usuario.
     * @return UltimoProgresoLibroDTO.
     */
    @Transactional(readOnly = true)
    public UltimoProgresoLibroDTO getUltimoActualizado(Long usuarioId) {
        List<UsuarioLibro> lista = usuarioLibroRepository.findUltimosPorFechaActualizacion(usuarioId, PageRequest.of(0, 1));
        if (lista.isEmpty()) {
            return null;
        }
        UsuarioLibro ul = lista.get(0);
        Libro libro = ul.getLibro();
        if (libro == null) {
            return null;
        }
        UltimoProgresoLibroDTO dto = new UltimoProgresoLibroDTO();
        dto.setLibroId(libro.getId());
        dto.setTitulo(libro.getTitulo());
        dto.setPortada(libro.getPortada());
        int pag = ul.getPaginaActual() != null ? ul.getPaginaActual() : 0;
        dto.setPaginaActual(pag);
        dto.setPaginasTotales(libro.getNumPaginas());
        dto.setPorcentaje(calcularPorcentaje(pag, libro.getNumPaginas()));
        dto.setEstado(ul.getEstado() != null ? ul.getEstado().name() : EstadoLectura.quiero_leer.name());
        dto.setFechaActualizacion(ul.getFechaActualizacion());
        return dto;
    }
}
