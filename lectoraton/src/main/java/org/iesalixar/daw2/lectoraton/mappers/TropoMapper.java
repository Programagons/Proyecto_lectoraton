package org.iesalixar.daw2.lectoraton.mappers;

import org.iesalixar.daw2.lectoraton.dtos.TropoCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.TropoDTO;
import org.iesalixar.daw2.lectoraton.entities.Tropo;
import org.springframework.stereotype.Component;

@Component
public class TropoMapper {

    public TropoDTO toDTO(Tropo tropo) {
        TropoDTO dto = new TropoDTO();
        dto.setId(tropo.getId());
        dto.setNombre(tropo.getNombre());
        dto.setDescripcion(tropo.getDescripcion());
        return dto;
    }

    public Tropo toEntity(TropoDTO dto) {
        Tropo tropo = new Tropo();
        tropo.setId(dto.getId());
        tropo.setNombre(dto.getNombre());
        tropo.setDescripcion(dto.getDescripcion());
        return tropo;
    }

    public Tropo toEntity(TropoCreateDTO createDTO) {
        Tropo tropo = new Tropo();
        tropo.setNombre(createDTO.getNombre());
        tropo.setDescripcion(createDTO.getDescripcion());
        return tropo;
    }
}
