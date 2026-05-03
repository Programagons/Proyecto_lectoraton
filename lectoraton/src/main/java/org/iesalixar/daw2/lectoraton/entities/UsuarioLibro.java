package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa el estado de lectura de un libro por un usuario.
 * Corresponde a la tabla "usuarios_libros" en el schema (clave compuesta).
 */
@Entity
@Table(name = "usuarios_libros")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLibro {

    @EmbeddedId
    private UsuarioLibroId id = new UsuarioLibroId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("libroId")
    @JoinColumn(name = "libro_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Libro libro;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoLectura estado;

    @Column(name = "pagina_actual")
    private Integer paginaActual;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "calificacion")
    private Integer calificacion; // 1-5

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
