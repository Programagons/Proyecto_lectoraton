package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UltimoProgresoLibroDTO {
    private Long libroId;
    private String titulo;
    private String portada;
    private int paginaActual;
    private Integer paginasTotales;
    private double porcentaje;
    private String estado;
    private LocalDateTime fechaActualizacion;
}
