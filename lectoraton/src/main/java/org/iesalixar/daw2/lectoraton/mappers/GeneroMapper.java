package org.iesalixar.daw2.lectoraton.mappers;

import org.iesalixar.daw2.lectoraton.dtos.GeneroCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.GeneroDTO;
import org.iesalixar.daw2.lectoraton.entities.Genero;
import org.springframework.stereotype.Component;

@Component
public class GeneroMapper {

    public GeneroDTO toDTO(Genero genero) {
        GeneroDTO dto = new GeneroDTO();
        dto.setId(genero.getId());
        dto.setNombre(genero.getNombre());
        dto.setDescripcion(genero.getDescripcion());
        return dto;
    }

    public Genero toEntity(GeneroDTO dto) {
        Genero genero = new Genero();
        genero.setId(dto.getId());
        genero.setNombre(dto.getNombre());
        genero.setDescripcion(dto.getDescripcion());
        return genero;
    }

    public Genero toEntity(GeneroCreateDTO createDTO) {
        Genero genero = new Genero();
        genero.setNombre(createDTO.getNombre());
        genero.setDescripcion(createDTO.getDescripcion());
        return genero;
    }
}
