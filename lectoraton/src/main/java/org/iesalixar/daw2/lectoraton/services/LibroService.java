package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.LibroCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroDTO;
import org.iesalixar.daw2.lectoraton.entities.Genero;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.entities.Tropo;
import org.iesalixar.daw2.lectoraton.mappers.LibroMapper;
import org.iesalixar.daw2.lectoraton.entities.Autor;
import org.iesalixar.daw2.lectoraton.repositories.AutorRepository;
import org.iesalixar.daw2.lectoraton.repositories.GeneroRepository;
import org.iesalixar.daw2.lectoraton.repositories.LibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.TropoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LibroService {

    private static final Logger logger = LoggerFactory.getLogger(LibroService.class);

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final GeneroRepository generoRepository;
    private final TropoRepository tropoRepository;
    private final LibroMapper libroMapper;
    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;

    public LibroService(LibroRepository libroRepository,
                        AutorRepository autorRepository,
                        GeneroRepository generoRepository,
                        TropoRepository tropoRepository,
                        LibroMapper libroMapper,
                        FileStorageService fileStorageService,
                        MessageSource messageSource) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.generoRepository = generoRepository;
        this.tropoRepository = tropoRepository;
        this.libroMapper = libroMapper;
        this.fileStorageService = fileStorageService;
        this.messageSource = messageSource;
    }

    public Page<LibroDTO> getAllLibros(Pageable pageable) {
        return libroRepository.findAll(pageable).map(libroMapper::toDTO);
    }

    public Optional<LibroDTO> getLibroById(Long id) {
        return libroRepository.findById(id).map(libroMapper::toDTO);
    }

    public Optional<LibroDTO> getLibroByIsbn(String isbn) {
        return libroRepository.findByIsbn(isbn).map(libroMapper::toDTO);
    }

    public Page<LibroDTO> explorar(String titulo, String autor, Long autorId, Long generoId, Long tropoId, String saga, Pageable pageable) {
        return libroRepository.explorar(titulo, autor, autorId, generoId, tropoId, saga, pageable).map(libroMapper::toDTO);
    }

    public List<LibroDTO> listarNovedades(int size) {
        int n = Math.min(Math.max(size, 1), 48);
        return libroRepository.findNovedades(PageRequest.of(0, n)).stream()
                .map(libroMapper::toDTO)
                .toList();
    }

    public List<LibroDTO> listarMasLeidosPorCalificaciones(int size) {
        int n = Math.min(Math.max(size, 1), 48);
        List<Long> ids = libroRepository.findLibroIdsMasCalificaciones(n);
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Libro> byId = libroRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Libro::getId, Function.identity()));
        List<LibroDTO> out = new ArrayList<>();
        for (Long id : ids) {
            Libro l = byId.get(id);
            if (l != null) {
                out.add(libroMapper.toDTO(l));
            }
        }
        return out;
    }

    public LibroDTO createLibro(LibroCreateDTO dto, Locale locale) {
        if (libroRepository.existsByIsbn(dto.getIsbn())) {
            String msg = messageSource.getMessage("msg.libro.isbn.existe", null, locale);
            throw new IllegalArgumentException(msg != null ? msg : "El ISBN ya existe.");
        }
        String portadaFileName = null;
        if (dto.getPortadaFile() != null && !dto.getPortadaFile().isEmpty()) {
            portadaFileName = fileStorageService.saveFile(dto.getPortadaFile());
            if (portadaFileName == null) throw new RuntimeException("Error al guardar la portada.");
        }
        Libro libro = libroMapper.toEntity(dto, portadaFileName);
        setGenerosAndTropos(libro, dto.getGeneroIds(), dto.getTropoIds());
        Libro saved = libroRepository.save(libro);
        logger.info("Libro creado con ID {}", saved.getId());
        return libroMapper.toDTO(saved);
    }

    public LibroDTO updateLibro(Long id, LibroCreateDTO dto, Locale locale) {
        Libro existing = libroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));
        if (libroRepository.existsByIsbnAndIdNot(dto.getIsbn(), id)) {
            String msg = messageSource.getMessage("msg.libro.isbn.existe", null, locale);
            throw new IllegalArgumentException(msg != null ? msg : "El ISBN ya existe.");
        }
        String portadaFileName = existing.getPortada();
        if (dto.getPortadaFile() != null && !dto.getPortadaFile().isEmpty()) {
            portadaFileName = fileStorageService.saveFile(dto.getPortadaFile());
            if (portadaFileName == null) throw new RuntimeException("Error al guardar la portada.");
        }
        existing.setIsbn(dto.getIsbn());
        existing.setTitulo(dto.getTitulo());
        existing.setSagaNombre(dto.getSagaNombre());
        existing.setNumeroSaga(dto.getNumeroSaga());
        existing.setSinopsis(dto.getSinopsis());
        existing.setNumPaginas(dto.getNumPaginas());
        existing.setFechaPublicacion(dto.getFechaPublicacion());
        existing.setPortada(portadaFileName);
        Autor autor = autorRepository.findById(dto.getAutorId())
                .orElseThrow(() -> new IllegalArgumentException("Autor no encontrado."));
        existing.setAutor(autor);
        setGenerosAndTropos(existing, dto.getGeneroIds(), dto.getTropoIds());
        Libro updated = libroRepository.save(existing);
        return libroMapper.toDTO(updated);
    }

    public void deleteLibro(Long id) {
        Libro libro = libroRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));
        if (libro.getPortada() != null && !libro.getPortada().isEmpty()) {
            fileStorageService.deleteFile(libro.getPortada());
        }
        libroRepository.deleteById(id);
        logger.info("Libro con ID {} eliminado.", id);
    }

    private void setGenerosAndTropos(Libro libro, Set<Long> generoIds, Set<Long> tropoIds) {
        if (generoIds != null && !generoIds.isEmpty()) {
            List<Genero> generos = generoRepository.findAllById(generoIds);
            libro.setGeneros(new HashSet<>(generos));
        } else {
            libro.setGeneros(new HashSet<>());
        }
        if (tropoIds != null && !tropoIds.isEmpty()) {
            List<Tropo> tropos = tropoRepository.findAllById(tropoIds);
            libro.setTropos(new HashSet<>(tropos));
        } else {
            libro.setTropos(new HashSet<>());
        }
    }
}
