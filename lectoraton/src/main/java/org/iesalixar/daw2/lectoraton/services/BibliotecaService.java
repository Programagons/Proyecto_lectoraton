package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.BibliotecaCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.BibliotecaDTO;
import org.iesalixar.daw2.lectoraton.dtos.BibliotecaRenameDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroDTO;
import org.iesalixar.daw2.lectoraton.entities.Biblioteca;
import org.iesalixar.daw2.lectoraton.entities.BibliotecaLibro;
import org.iesalixar.daw2.lectoraton.entities.BibliotecaLibroId;
import org.iesalixar.daw2.lectoraton.entities.EstadoLectura;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibro;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibroId;
import org.iesalixar.daw2.lectoraton.mappers.BibliotecaMapper;
import org.iesalixar.daw2.lectoraton.mappers.LibroMapper;
import org.iesalixar.daw2.lectoraton.repositories.BibliotecaLibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.BibliotecaRepository;
import org.iesalixar.daw2.lectoraton.repositories.LibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioLibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BibliotecaService {
    private static final List<String> BIBLIOTECAS_FIJAS = List.of("Leyendo", "Leído", "Por Leer");

    private final BibliotecaRepository bibliotecaRepository;
    private final BibliotecaLibroRepository bibliotecaLibroRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final UsuarioLibroRepository usuarioLibroRepository;
    private final BibliotecaMapper bibliotecaMapper;
    private final LibroMapper libroMapper;

    public BibliotecaService(BibliotecaRepository bibliotecaRepository,
                            BibliotecaLibroRepository bibliotecaLibroRepository,
                            UsuarioRepository usuarioRepository,
                            LibroRepository libroRepository,
                            UsuarioLibroRepository usuarioLibroRepository,
                            BibliotecaMapper bibliotecaMapper,
                            LibroMapper libroMapper) {
        this.bibliotecaRepository = bibliotecaRepository;
        this.bibliotecaLibroRepository = bibliotecaLibroRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.usuarioLibroRepository = usuarioLibroRepository;
        this.bibliotecaMapper = bibliotecaMapper;
        this.libroMapper = libroMapper;
    }

    /**
     * Obtiene las bibliotecas de un usuario.
     * @param usuarioId ID del usuario.
     * @return Lista de BibliotecaDTO.
     */
    public List<BibliotecaDTO> getByUsuarioId(Long usuarioId) { 
        return bibliotecaRepository.findByUsuarioIdOrderByNombreAsc(usuarioId).stream()
                .map(bibliotecaMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una biblioteca por su ID.
     * @param id ID de la biblioteca.
     * @return Optional de BibliotecaDTO.
     */
    public Optional<BibliotecaDTO> getById(Long id) {
        return bibliotecaRepository.findById(id).map(bibliotecaMapper::toDTO);
    }

    /**
     * Obtiene los libros de una biblioteca y un usuario.
     * @param bibliotecaId ID de la biblioteca.
     * @param usuarioId ID del usuario.
     * @return Lista de LibroDTO.
     */
    public List<LibroDTO> getLibrosByBibliotecaIdAndUsuarioId(Long bibliotecaId, Long usuarioId) {
        Biblioteca biblioteca = bibliotecaRepository.findByIdAndUsuarioId(bibliotecaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Biblioteca no encontrada."));

        return biblioteca.getLibrosEnBiblioteca().stream()
                .map(BibliotecaLibro::getLibro)
                .filter(libro -> libro != null)
                .sorted(Comparator.comparing(Libro::getTitulo, String.CASE_INSENSITIVE_ORDER))
                .map(libroMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva biblioteca.
     * @param dto DTO con los datos de la nueva biblioteca.
     * @return BibliotecaDTO.
     */
    public BibliotecaDTO create(BibliotecaCreateDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        String nombreNormalizado = dto.getNombre() == null ? "" : dto.getNombre().trim();
        if (bibliotecaRepository.existsByUsuarioIdAndNombreIgnoreCase(usuario.getId(), nombreNormalizado)) {
            throw new IllegalArgumentException("Ya tienes una biblioteca con ese nombre.");
        }
        dto.setNombre(nombreNormalizado);
        Biblioteca biblioteca = bibliotecaMapper.toEntity(dto, usuario);
        biblioteca = bibliotecaRepository.save(biblioteca);
        if (dto.getLibroIds() != null && !dto.getLibroIds().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Long libroId : dto.getLibroIds()) {
                Libro libro = libroRepository.findById(libroId).orElse(null);
                if (libro != null) {
                    BibliotecaLibro bl = new BibliotecaLibro();
                    bl.setId(new BibliotecaLibroId(biblioteca.getId(), libroId));
                    bl.setBiblioteca(biblioteca);
                    bl.setLibro(libro);
                    bl.setFechaAgregado(now);
                    biblioteca.getLibrosEnBiblioteca().add(bl);
                }
            }
            biblioteca = bibliotecaRepository.save(biblioteca);
        }
        return bibliotecaMapper.toDTO(biblioteca);
    }

    /**
     * Renombra una biblioteca.
     * @param id ID de la biblioteca.
     * @param usuarioId ID del usuario.
     * @param dto DTO con los datos de la nueva biblioteca.
     * @return BibliotecaDTO.
     */
    public BibliotecaDTO rename(Long id, Long usuarioId, BibliotecaRenameDTO dto) {
        Biblioteca biblioteca = bibliotecaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Biblioteca no encontrada."));
        if (esBibliotecaFija(biblioteca.getNombre())) {
            throw new IllegalArgumentException("Esta biblioteca es fija y no se puede renombrar.");
        }
        String nombreNormalizado = dto.getNombre() == null ? "" : dto.getNombre().trim();
        if (bibliotecaRepository.existsByUsuarioIdAndNombreIgnoreCaseAndIdNot(usuarioId, nombreNormalizado, id)) {
            throw new IllegalArgumentException("Ya tienes una biblioteca con ese nombre.");
        }
        biblioteca.setNombre(nombreNormalizado);
        return bibliotecaMapper.toDTO(bibliotecaRepository.save(biblioteca));
    }

    /**
     * Elimina una biblioteca.
     * @param id ID de la biblioteca.
     * @param usuarioId ID del usuario.
     */
    public void delete(Long id, Long usuarioId) {
        Biblioteca biblioteca = bibliotecaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Biblioteca no encontrada."));
        if (esBibliotecaFija(biblioteca.getNombre())) {
            throw new IllegalArgumentException("Esta biblioteca es fija y no se puede eliminar.");
        }
        bibliotecaRepository.delete(biblioteca);
    }

    /**
     * Agrega un libro a una biblioteca.
     * @param bibliotecaId ID de la biblioteca.
     * @param usuarioId ID del usuario.
     * @param libroId ID del libro.
     */
    public void addLibro(Long bibliotecaId, Long usuarioId, Long libroId) {
        Biblioteca biblioteca = bibliotecaRepository.findByIdAndUsuarioId(bibliotecaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Biblioteca no encontrada."));
        if (bibliotecaLibroRepository.existsByBibliotecaIdAndLibroId(bibliotecaId, libroId)) {
            throw new IllegalArgumentException("El libro ya está en esta biblioteca.");
        }
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));
        BibliotecaLibro bl = new BibliotecaLibro();
        bl.setId(new BibliotecaLibroId(bibliotecaId, libroId));
        bl.setBiblioteca(biblioteca);
        bl.setLibro(libro);
        bl.setFechaAgregado(LocalDateTime.now());
        bibliotecaLibroRepository.save(bl);
        sincronizarEstadoLecturaSiBibliotecaEsFija(biblioteca, libro, usuarioId);
    }

    /**
     * Crea las bibliotecas fijas si faltan.
     * @param usuario Usuario.
     */
    public void crearBibliotecasFijasSiFaltan(Usuario usuario) {
        for (String nombre : BIBLIOTECAS_FIJAS) {
            if (!bibliotecaRepository.existsByUsuarioIdAndNombreIgnoreCase(usuario.getId(), nombre)) {
                Biblioteca biblioteca = new Biblioteca();
                biblioteca.setUsuario(usuario);
                biblioteca.setNombre(nombre);
                bibliotecaRepository.save(biblioteca);
            }
        }
    }

    /**
     * Verifica si una biblioteca es fija.
     * @param nombre Nombre de la biblioteca.
     * @return True si es fija, false en caso contrario.
     */
    private boolean esBibliotecaFija(String nombre) {
        return BIBLIOTECAS_FIJAS.stream().anyMatch(fija -> fija.equalsIgnoreCase(nombre));
    }

    /**
     * Sincroniza el estado de lectura si la biblioteca es fija.
     * @param biblioteca Biblioteca.
     * @param libro Libro.
     * @param usuarioId ID del usuario.
     */
    private void sincronizarEstadoLecturaSiBibliotecaEsFija(Biblioteca biblioteca, Libro libro, Long usuarioId) {
        EstadoLectura estado = getEstadoLecturaPorBiblioteca(biblioteca.getNombre());
        if (estado == null) {
            return;
        }

        UsuarioLibro usuarioLibro = usuarioLibroRepository.findByUsuarioIdAndLibroId(usuarioId, libro.getId())
                .orElseGet(() -> {
                    UsuarioLibro nuevo = new UsuarioLibro();
                    nuevo.setId(new UsuarioLibroId(usuarioId, libro.getId()));
                    nuevo.setUsuario(biblioteca.getUsuario());
                    nuevo.setLibro(libro);
                    return nuevo;
                });

        usuarioLibro.setEstado(estado);
        usuarioLibroRepository.save(usuarioLibro);
    }

    /**
     * Obtiene el estado de lectura por el nombre de la biblioteca.
     * @param nombreBiblioteca Nombre de la biblioteca.
     * @return EstadoLectura.
     */
    private EstadoLectura getEstadoLecturaPorBiblioteca(String nombreBiblioteca) {
        if ("Leyendo".equalsIgnoreCase(nombreBiblioteca)) {
            return EstadoLectura.leyendo;
        }
        if ("Leído".equalsIgnoreCase(nombreBiblioteca)) {
            return EstadoLectura.leido;
        }
        if ("Por Leer".equalsIgnoreCase(nombreBiblioteca)) {
            return EstadoLectura.quiero_leer;
        }
        return null;
    }
}
