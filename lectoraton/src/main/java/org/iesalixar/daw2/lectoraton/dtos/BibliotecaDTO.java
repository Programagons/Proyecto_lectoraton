package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class BibliotecaDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String nombre;
    private Set<Long> libroIds;
    /** URL de la portada del libro añadido más recientemente a la biblioteca (por fecha_agregado). */
    private String ultimaPortada;
}
