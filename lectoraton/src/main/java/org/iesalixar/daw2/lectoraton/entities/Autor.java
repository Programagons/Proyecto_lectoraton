package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity // Marca esta clase como una entidad gestionada por JPA.
@Table(name = "autores") // Especifica el nombre de la tabla asociada a esta entidad.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Autor {

    // Campo que almacena el identificador único del Autor.
    // Es una clave primaria autogenerada por la base de datos.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campo que almacena el nombre del autor, como "Manuel López Vargas"
    @Column(name = "nombre_completo", nullable = false, length = 150) // Define la columna correspondiente en la tabla.
    private String nombreCompleto;

    // Campo que almacena la nacionalidad del autor, como "España"
    @Column(name = "nacionalidad", nullable = false, length = 100) // Define la columna correspondiente en la tabla.
    private String nacionalidad;

    // Campo para fotos del autor
    @Column(name = "image")
    private String image;


    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Libro> libros;


    /**
     * Este es un constructor personalizado que no incluye el campo `id`.
     * Se utiliza para crear instancias de `Autor` cuando no es necesario o no
     * se conoce el `id` del autor
     * `id` es autogenerado.
     *
     * @param nombreCompleto Nombre del Autor.
     * @param nacionalidad Nacionalidad del Autor.
     */
    public Autor(String nombreCompleto, String nacionalidad) {
        this.nombreCompleto = nombreCompleto;
        this.nacionalidad = nacionalidad;
    }
}