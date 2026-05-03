package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TropoCreateDTO {

    @NotEmpty(message = "{msg.tropo.nombre.notEmpty}")
    @Size(max = 50, message = "{msg.tropo.nombre.size}")
    private String nombre;

    @Size(max = 255, message = "{msg.tropo.descripcion.size}")
    private String descripcion;
}
