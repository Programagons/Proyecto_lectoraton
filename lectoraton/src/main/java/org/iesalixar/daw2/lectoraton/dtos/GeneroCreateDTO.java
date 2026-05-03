package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneroCreateDTO {

    @NotEmpty(message = "{msg.genero.nombre.notEmpty}")
    @Size(max = 50, message = "{msg.genero.nombre.size}")
    private String nombre;

    @Size(max = 255, message = "{msg.genero.descripcion.size}")
    private String descripcion;
}
