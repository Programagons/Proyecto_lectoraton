package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgresoLecturaUpdateDTO {

    @NotNull(message = "paginaActual es obligatoria")
    @Min(value = 0, message = "paginaActual no puede ser negativa")
    private Integer paginaActual;

    /** Opcional: quiero_leer, leyendo, leido, abandonado */
    private String estado;
}
