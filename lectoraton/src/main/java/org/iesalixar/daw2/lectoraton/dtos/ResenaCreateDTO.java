package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResenaCreateDTO {

    private Long usuarioId;

    @NotNull(message = "{msg.resena.libroId.notNull}")
    private Long libroId;

    @Size(max = 200, message = "{msg.resena.titulo.size}")
    private String titulo;

    private String contenido;

    private Boolean contieneSpoiler;

    @Min(1)
    @Max(5)
    private Integer calificacion;
}
