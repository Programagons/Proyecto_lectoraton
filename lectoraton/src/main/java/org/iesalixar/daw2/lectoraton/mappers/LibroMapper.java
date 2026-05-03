package org.iesalixar.daw2.lectoraton.mappers;

import org.iesalixar.daw2.lectoraton.dtos.LibroCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroDTO;
import org.iesalixar.daw2.lectoraton.entities.Autor;
import org.iesalixar.daw2.lectoraton.entities.Genero;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.entities.Tropo;
import org.iesalixar.daw2.lectoraton.repositories.AutorRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LibroMapper {

    private final AutorRepository autorRepository;

    public LibroMapper(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    public LibroDTO toDTO(Libro libro) {
        LibroDTO dto = new LibroDTO();
        dto.setId(libro.getId());
        dto.setIsbn(libro.getIsbn());
        dto.setTitulo(libro.getTitulo());
        dto.setSagaNombre(libro.getSagaNombre());
        dto.setNumeroSaga(libro.getNumeroSaga());
        dto.setSinopsis(libro.getSinopsis());
        if (libro.getAutor() != null) {
            dto.setAutorId(libro.getAutor().getId());
            dto.setAutorNombre(libro.getAutor().getNombreCompleto());
        }
        dto.setNumPaginas(libro.getNumPaginas());
        dto.setFechaPublicacion(libro.getFechaPublicacion());
        dto.setPortada(libro.getPortada());
        if (libro.getGeneros() != null) {
            dto.setGeneroIds(libro.getGeneros().stream().map(Genero::getId).collect(Collectors.toSet()));
            dto.setGeneroNombres(libro.getGeneros().stream().map(Genero::getNombre).collect(Collectors.toSet()));
        }
        if (libro.getTropos() != null) {
            dto.setTropoIds(libro.getTropos().stream().map(Tropo::getId).collect(Collectors.toSet()));
            dto.setTropoNombres(libro.getTropos().stream().map(Tropo::getNombre).collect(Collectors.toSet()));
        }
        return dto;
    }

    public Libro toEntity(LibroDTO dto) {
        Libro libro = new Libro();
        libro.setId(dto.getId());
        libro.setIsbn(dto.getIsbn());
        libro.setTitulo(dto.getTitulo());
        libro.setSagaNombre(dto.getSagaNombre());
        libro.setNumeroSaga(dto.getNumeroSaga());
        libro.setSinopsis(dto.getSinopsis());
        if (dto.getAutorId() != null) {
            Autor autor = autorRepository.findById(dto.getAutorId()).orElse(null);
            libro.setAutor(autor);
        }
        libro.setNumPaginas(dto.getNumPaginas());
        libro.setFechaPublicacion(dto.getFechaPublicacion());
        libro.setPortada(dto.getPortada());
        return libro;
    }

    public Libro toEntity(LibroCreateDTO createDTO, String portadaFileName) {
        Libro libro = new Libro();
        libro.setIsbn(createDTO.getIsbn());
        libro.setTitulo(createDTO.getTitulo());
        libro.setSagaNombre(createDTO.getSagaNombre());
        libro.setNumeroSaga(createDTO.getNumeroSaga());
        libro.setSinopsis(createDTO.getSinopsis());
        libro.setNumPaginas(createDTO.getNumPaginas());
        libro.setFechaPublicacion(createDTO.getFechaPublicacion());
        libro.setPortada(portadaFileName);
        Autor autor = autorRepository.findById(createDTO.getAutorId())
                .orElseThrow(() -> new IllegalArgumentException("Autor no encontrado"));
        libro.setAutor(autor);
        libro.setGeneros(new HashSet<>());
        libro.setTropos(new HashSet<>());
        return libro;
    }
}
