package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Perfil del propio usuario: datos habituales + etiquetas legibles de roles (sin datos sensibles).
 */
@Getter
@Setter
public class UsuarioPerfilDTO {

    private Long id;
    private String username;
    private String nombre;
    private String apellidos;
    private String email;
    private String bio;
    private String icono;

    /** Textos para mostrar en UI: "Lector", "Editor", "Administrador", … */
    private List<String> rolesEtiqueta = new ArrayList<>();
}
