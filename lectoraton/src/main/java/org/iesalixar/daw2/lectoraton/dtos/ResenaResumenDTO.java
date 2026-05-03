package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ResenaResumenDTO {
    private Double mediaCalificaciones;
    private Long totalCalificaciones;
    private Long totalResenas;
    private Map<Integer, Long> distribucionEstrellas;
}
