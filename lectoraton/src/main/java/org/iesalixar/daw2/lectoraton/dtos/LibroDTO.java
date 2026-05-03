package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO que representa un libro para la API.
 */
@Getter
@Setter
public class LibroDTO {

    private Long id;
    private String isbn;
    private String titulo;
    private String sagaNombre;
    private Integer numeroSaga;
    private String sinopsis;
    private Long autorId;
    private String autorNombre;
    private Integer numPaginas;
    private LocalDate fechaPublicacion;
    private String portada;
    private Set<Long> generoIds;
    private Set<String> generoNombres;
    private Set<Long> tropoIds;
    private Set<String> tropoNombres;
}
