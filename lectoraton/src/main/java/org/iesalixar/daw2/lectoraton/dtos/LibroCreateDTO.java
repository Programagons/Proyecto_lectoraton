package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO para crear o actualizar un libro.
 */
@Getter
@Setter
public class LibroCreateDTO {

    @NotEmpty(message = "{msg.libro.isbn.notEmpty}")
    @Size(max = 13, message = "{msg.libro.isbn.size}")
    private String isbn;

    @NotEmpty(message = "{msg.libro.titulo.notEmpty}")
    @Size(max = 200, message = "{msg.libro.titulo.size}")
    private String titulo;

    @Size(max = 150)
    private String sagaNombre;

    private Integer numeroSaga;

    private String sinopsis;

    @NotNull(message = "{msg.libro.autorId.notNull}")
    private Long autorId;

    private Integer numPaginas;
    private LocalDate fechaPublicacion;

    private MultipartFile portadaFile;

    /** IDs de géneros a asociar. */
    private Set<Long> generoIds;

    /** IDs de tropos a asociar. */
    private Set<Long> tropoIds;
}
