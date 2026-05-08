package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FeedItemDTO {
    private Long id;
    private String tipo;
    private LocalDateTime fechaCreacion;
    private Long actorId;
    private String actorUsername;
    private String actorNombreCompleto;
    private String actorIcono;
    private Long libroId;
    private String libroTitulo;
    private String libroPortada;
    /** Total de páginas del libro (si está cargado); útil para barras de progreso en el feed. */
    private Integer libroNumPaginas;
    private Long resenaId;
    private String texto;
    private Boolean contieneSpoiler;
}
