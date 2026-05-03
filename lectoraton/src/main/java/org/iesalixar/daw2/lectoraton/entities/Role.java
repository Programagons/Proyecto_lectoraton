package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/**
 * La clase Role representa un rol o autoridad en el sistema.
 * Trabaja con la entidad Usuario para autorización y control de acceso.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "usuarios")
@EqualsAndHashCode(exclude = "usuarios")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "{msg.role.nombre.notEmpty}")
    @Size(max = 50, message = "{msg.role.nombre.size}")
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<Usuario> usuarios;

    public Role(String nombre) {
        this.nombre = nombre;
    }
}