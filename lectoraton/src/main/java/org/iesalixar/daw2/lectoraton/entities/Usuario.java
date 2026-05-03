package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad única de usuario del sistema (tabla "usuarios").
 * Usada para autenticación/seguridad, reseñas, comentarios, bibliotecas, etc.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "bio", length = 255)
    private String bio;

    @Column(name = "icono", length = 255)
    private String icono;

    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "fecha_modificacion_password")
    private LocalDateTime fechaModificacionPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarios_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();

    /** Usuarios que sigue este usuario (seguidor_id = this, seguido_id = otro). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuarios_seguidores",
            joinColumns = @JoinColumn(name = "seguidor_id"),
            inverseJoinColumns = @JoinColumn(name = "seguido_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Usuario> seguidos = new HashSet<>();

    /** Usuarios que siguen a este usuario (seguido_id = this). */
    @ManyToMany(mappedBy = "seguidos", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Usuario> seguidores = new HashSet<>();
}
