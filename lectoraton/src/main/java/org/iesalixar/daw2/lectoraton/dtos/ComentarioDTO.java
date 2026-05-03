package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ComentarioDTO {

    private Long id;
    private Long resenaId;
    private Long usuarioId;
    private String usuarioNombre;
    private String contenido;
    private Boolean contieneSpoiler;
    private LocalDateTime fechaCreacion;
}
