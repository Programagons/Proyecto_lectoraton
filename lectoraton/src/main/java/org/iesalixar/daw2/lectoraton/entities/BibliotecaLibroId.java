package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Clave compuesta para la relación biblioteca-libro (biblioteca_id, libro_id).
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BibliotecaLibroId implements Serializable {

    private Long bibliotecaId;
    private Long libroId;
}
