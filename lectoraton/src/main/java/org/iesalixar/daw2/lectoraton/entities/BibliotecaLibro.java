package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un libro dentro de una biblioteca (con fecha de alta).
 * Corresponde a la tabla "bibliotecas_libros" en el schema.
 */
@Entity
@Table(name = "bibliotecas_libros")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BibliotecaLibro {

    @EmbeddedId
    private BibliotecaLibroId id = new BibliotecaLibroId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bibliotecaId")
    @JoinColumn(name = "biblioteca_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Biblioteca biblioteca;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libroId")
    @JoinColumn(name = "libro_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Libro libro;

    @Column(name = "fecha_agregado")
    private LocalDateTime fechaAgregado;
}
