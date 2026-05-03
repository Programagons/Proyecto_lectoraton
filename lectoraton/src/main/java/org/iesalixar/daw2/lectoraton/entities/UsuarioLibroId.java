package org.iesalixar.daw2.lectoraton.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Clave compuesta para la entidad UsuarioLibro (usuario_id, libro_id).
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLibroId implements Serializable {

    private Long usuarioId;
    private Long libroId;
}
