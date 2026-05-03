package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioCreateDTO {

    @NotEmpty(message = "{msg.usuario.username.notEmpty}")
    @Size(max = 50)
    private String username;

    @NotEmpty(message = "{msg.usuario.password.notEmpty}")
    @Size(min = 8)
    private String password;

    @NotEmpty(message = "{msg.usuario.nombre.notEmpty}")
    @Size(max = 50)
    private String nombre;

    @NotEmpty(message = "{msg.usuario.apellidos.notEmpty}")
    @Size(max = 100)
    private String apellidos;

    @NotEmpty(message = "{msg.usuario.email.notEmpty}")
    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 255)
    private String bio;

    @Size(max = 255)
    private String icono;
}
