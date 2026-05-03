package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO que representa un usuario (sin contraseña).
 * Usado para exponer datos en la API.
 */
@Getter
@Setter
public class UsuarioDTO {

    private Long id;
    private String username;
    private String nombre;
    private String apellidos;
    private String email;
    private String bio;
    private String icono;
}
