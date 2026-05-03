package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LibroRecomendacionDTO {
    private LibroMiniDTO origen;
    /** null si no hay ninguna sugerencia razonable en el catálogo */
    private LibroMiniDTO recomendado;
    private String explicacion;
}
