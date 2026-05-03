package org.iesalixar.daw2.lectoraton.mappers;

import org.iesalixar.daw2.lectoraton.dtos.BibliotecaCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.BibliotecaDTO;
import org.iesalixar.daw2.lectoraton.entities.Biblioteca;
import org.iesalixar.daw2.lectoraton.entities.BibliotecaLibro;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BibliotecaMapper {

    public BibliotecaDTO toDTO(Biblioteca biblioteca) {
        BibliotecaDTO dto = new BibliotecaDTO();
        dto.setId(biblioteca.getId());
        if (biblioteca.getUsuario() != null) {
            dto.setUsuarioId(biblioteca.getUsuario().getId());
            dto.setUsuarioNombre(biblioteca.getUsuario().getNombre() + " " + biblioteca.getUsuario().getApellidos());
        }
        dto.setNombre(biblioteca.getNombre());
        if (biblioteca.getLibrosEnBiblioteca() != null && !biblioteca.getLibrosEnBiblioteca().isEmpty()) {
            Set<Long> ids = biblioteca.getLibrosEnBiblioteca().stream()
                    .map(BibliotecaLibro::getLibro)
                    .map(Libro::getId)
                    .collect(Collectors.toSet());
            dto.setLibroIds(ids);
            dto.setUltimaPortada(resolverUltimaPortada(biblioteca));
        } else {
            dto.setLibroIds(Collections.emptySet());
            dto.setUltimaPortada(null);
        }
        return dto;
    }

    public Biblioteca toEntity(BibliotecaCreateDTO dto, org.iesalixar.daw2.lectoraton.entities.Usuario usuario) {
        Biblioteca biblioteca = new Biblioteca();
        biblioteca.setUsuario(usuario);
        biblioteca.setNombre(dto.getNombre());
        return biblioteca;
    }

    /**
     * Portada del volumen más reciente según {@code fecha_agregado}; si falta la fecha, se usa el mayor id de libro.
     */
    private String resolverUltimaPortada(Biblioteca biblioteca) {
        Optional<BibliotecaLibro> elegido = biblioteca.getLibrosEnBiblioteca().stream()
                .max(Comparator
                        .comparing((BibliotecaLibro bl) -> bl.getFechaAgregado() != null
                                ? bl.getFechaAgregado()
                                : LocalDateTime.MIN)
                        .thenComparing(bl -> bl.getLibro() != null ? bl.getLibro().getId() : 0L));
        return elegido.map(BibliotecaLibro::getLibro).map(Libro::getPortada).orElse(null);
    }
}
