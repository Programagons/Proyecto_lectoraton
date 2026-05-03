package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioMiniDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String icono;
}
