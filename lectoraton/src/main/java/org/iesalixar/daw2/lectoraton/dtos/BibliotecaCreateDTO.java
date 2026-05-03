package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class BibliotecaCreateDTO {

    private Long usuarioId;

    @NotEmpty(message = "{msg.biblioteca.nombre.notEmpty}")
    @Size(max = 100, message = "{msg.biblioteca.nombre.size}")
    private String nombre;

    private Set<Long> libroIds;
}
