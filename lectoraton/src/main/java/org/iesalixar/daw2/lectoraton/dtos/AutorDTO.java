package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;


/**
 * ClaseDTO (Data Transfer Object) que representa un autor.
 *
 * Esta clase se utiliza para transferir datos de un autor
 * entre las capas de la aplicación, especialmente para exponerlos
 * a través de la API sin incluir información innecesaria o sensible.
 */

@Getter
@Setter
public class AutorDTO {

    /**
     * Identificador único del autor
     * Es el mismo ID que se encuentra en la entidad 'Autor' de la base de datos.
     */
    private long id;

    /**
     * Nombre Completo del autor.
     */
    private String nombre_completo;

    /**
     * Nacionalidad del autor.
     */
    private String nacionalidad;

    private String image;

}

