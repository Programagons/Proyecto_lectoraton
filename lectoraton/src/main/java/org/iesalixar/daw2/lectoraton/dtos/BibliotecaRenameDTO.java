package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BibliotecaRenameDTO {

    @NotEmpty(message = "{msg.biblioteca.nombre.notEmpty}")
    @Size(max = 100, message = "{msg.biblioteca.nombre.size}")
    private String nombre;
}
