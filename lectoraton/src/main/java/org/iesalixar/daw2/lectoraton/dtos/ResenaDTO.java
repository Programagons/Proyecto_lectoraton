package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ResenaDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioIcono;
    private Long libroId;
    private String libroTitulo;
    private String titulo;
    private String contenido;
    private Boolean contieneSpoiler;
    private Integer calificacion;
    private LocalDateTime fechaCreacion;
    private Long numLikes;
    private Long numComentarios;
    private boolean likedByCurrentUser;
}
