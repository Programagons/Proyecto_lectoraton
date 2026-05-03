package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una reseña de un libro por un usuario.
 * Corresponde a la tabla "resenas" en el schema.
 */
@Entity
@Table(name = "resenas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Libro libro;

    @Column(name = "titulo", length = 200)
    private String titulo;

    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "calificacion")
    private Integer calificacion;

    @Column(name = "contiene_spoiler")
    private Boolean contieneSpoiler = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "resena", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Comentario> comentarios = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "likes_resenas",
            joinColumns = @JoinColumn(name = "resena_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Usuario> usuariosQueDieronLike = new HashSet<>();
}
