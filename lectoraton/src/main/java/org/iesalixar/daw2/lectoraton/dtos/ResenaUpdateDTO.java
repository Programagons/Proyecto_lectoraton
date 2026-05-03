package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResenaUpdateDTO {

    @Size(max = 200, message = "{msg.resena.titulo.size}")
    private String titulo;

    private String contenido;

    private Boolean contieneSpoiler;

    @Min(1)
    @Max(5)
    private Integer calificacion;
}
