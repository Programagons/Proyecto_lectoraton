package org.iesalixar.daw2.lectoraton.mappers;

import org.iesalixar.daw2.lectoraton.dtos.ComentarioCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.ComentarioDTO;
import org.iesalixar.daw2.lectoraton.entities.Comentario;
import org.springframework.stereotype.Component;

@Component
public class ComentarioMapper {

    public ComentarioDTO toDTO(Comentario comentario) {
        ComentarioDTO dto = new ComentarioDTO();
        dto.setId(comentario.getId());
        if (comentario.getResena() != null) dto.setResenaId(comentario.getResena().getId());
        if (comentario.getUsuario() != null) {
            dto.setUsuarioId(comentario.getUsuario().getId());
            dto.setUsuarioNombre(comentario.getUsuario().getNombre() + " " + comentario.getUsuario().getApellidos());
        }
        dto.setContenido(comentario.getContenido());
        dto.setContieneSpoiler(Boolean.TRUE.equals(comentario.getContieneSpoiler()));
        dto.setFechaCreacion(comentario.getFechaCreacion());
        return dto;
    }

    public Comentario toEntity(ComentarioCreateDTO dto, org.iesalixar.daw2.lectoraton.entities.Resena resena, org.iesalixar.daw2.lectoraton.entities.Usuario usuario) {
        Comentario comentario = new Comentario();
        comentario.setResena(resena);
        comentario.setUsuario(usuario);
        comentario.setContenido(dto.getContenido());
        comentario.setContieneSpoiler(Boolean.TRUE.equals(dto.getContieneSpoiler()));
        return comentario;
    }
}
