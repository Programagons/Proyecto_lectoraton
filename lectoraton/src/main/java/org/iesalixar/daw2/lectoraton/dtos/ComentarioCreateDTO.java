package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComentarioCreateDTO {

    @NotNull(message = "{msg.comentario.resenaId.notNull}")
    private Long resenaId;

    private Long usuarioId;

    @NotEmpty(message = "{msg.comentario.contenido.notEmpty}")
    private String contenido;

    private Boolean contieneSpoiler;
}
