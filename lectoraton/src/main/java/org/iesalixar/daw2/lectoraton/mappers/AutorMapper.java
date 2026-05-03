package org.iesalixar.daw2.lectoraton.mappers;

import org.iesalixar.daw2.lectoraton.dtos.AutorCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.AutorDTO;
import org.iesalixar.daw2.lectoraton.entities.Autor;
import org.springframework.stereotype.Component;


@Component
public class AutorMapper {

    /**
     * Convierte una entidad 'Autor' a un 'AutorDTO'
     *
     * @param autor Entidad de autor.
     * @return DTO correspondiente.
     *
     */

    public AutorDTO toDTO(Autor autor){
        AutorDTO dto = new AutorDTO();
        dto.setId(autor.getId());
        dto.setNombre_completo(autor.getNombreCompleto());
        dto.setNacionalidad(autor.getNacionalidad());
        dto.setImage(autor.getImage());
        return dto;
    }


    /**
     * Convierte un 'AutorDTO' a una entidad 'Autor'
     *
     * @param dto DTO de autor.
     * @return Entidad Autor
     */

    public Autor toEntity(AutorDTO dto){
        Autor autor = new Autor();
        autor.setId(dto.getId());
        autor.setNombreCompleto(dto.getNombre_completo());
        autor.setNacionalidad(dto.getNacionalidad());
        autor.setImage(dto.getImage());
        return autor;
    }

    /**
     * Convierte un 'AutorCreateDTO' a una entidad 'Autor' (para creación)
     * @param createDTO para crear autores
     * @return Entidad autor
      */

    public Autor toEntity(AutorCreateDTO createDTO){
        Autor autor = new Autor();
        autor.setNombreCompleto(createDTO.getNombreCompleto());
        autor.setNacionalidad(createDTO.getNacionalidad());
        return autor;
    }


}
