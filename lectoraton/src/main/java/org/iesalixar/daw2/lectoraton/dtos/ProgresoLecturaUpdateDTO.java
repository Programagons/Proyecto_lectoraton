package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgresoLecturaUpdateDTO {

    @Min(value = 0, message = "paginaActual no puede ser negativa")
    private Integer paginaActual;

    @DecimalMin(value = "0.0", message = "porcentajeActual no puede ser negativo")
    @DecimalMax(value = "100.0", message = "porcentajeActual no puede ser mayor que 100")
    private Double porcentajeActual;

    /** Opcional: quiero_leer, leyendo, leido, abandonado */
    private String estado;
}
