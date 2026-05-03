package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un tropo literario (ej: enemies to lovers, chosen one).
 * Corresponde a la tabla "tropos" en el schema.
 */
@Entity
@Table(name = "tropos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tropo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @ManyToMany(mappedBy = "tropos", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Libro> libros = new HashSet<>();
}
