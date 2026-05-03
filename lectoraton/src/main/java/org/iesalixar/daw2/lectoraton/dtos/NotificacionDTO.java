package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificacionDTO {
    private String tipo; // like | comentario
    private String actorNombre;
    private String libroTitulo;
    private String resenaTitulo;
    private String mensaje;
    private LocalDateTime fecha;
}
