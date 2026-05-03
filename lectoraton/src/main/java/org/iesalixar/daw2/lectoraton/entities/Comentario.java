package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un comentario en una reseña.
 * Corresponde a la tabla "comentarios" en el schema.
 */
@Entity
@Table(name = "comentarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resena_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Resena resena;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "contiene_spoiler")
    private Boolean contieneSpoiler = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
