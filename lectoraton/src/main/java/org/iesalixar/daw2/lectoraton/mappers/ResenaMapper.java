package org.iesalixar.daw2.lectoraton.mappers;

import org.iesalixar.daw2.lectoraton.dtos.ResenaCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.ResenaDTO;
import org.iesalixar.daw2.lectoraton.entities.Resena;
import org.springframework.stereotype.Component;

@Component
public class ResenaMapper {

    public ResenaDTO toDTO(Resena resena) {
        ResenaDTO dto = new ResenaDTO();
        dto.setId(resena.getId());
        if (resena.getUsuario() != null) {
            dto.setUsuarioId(resena.getUsuario().getId());
            dto.setUsuarioNombre(resena.getUsuario().getNombre() + " " + resena.getUsuario().getApellidos());
            dto.setUsuarioIcono(resena.getUsuario().getIcono());
        }
        if (resena.getLibro() != null) {
            dto.setLibroId(resena.getLibro().getId());
            dto.setLibroTitulo(resena.getLibro().getTitulo());
        }
        dto.setTitulo(resena.getTitulo());
        dto.setContenido(resena.getContenido());
        dto.setContieneSpoiler(Boolean.TRUE.equals(resena.getContieneSpoiler()));
        dto.setCalificacion(resena.getCalificacion());
        dto.setFechaCreacion(resena.getFechaCreacion());
        if (resena.getUsuariosQueDieronLike() != null) {
            dto.setNumLikes((long) resena.getUsuariosQueDieronLike().size());
        } else {
            dto.setNumLikes(0L);
        }
        if (resena.getComentarios() != null) {
            dto.setNumComentarios((long) resena.getComentarios().size());
        } else {
            dto.setNumComentarios(0L);
        }
        return dto;
    }

    public Resena toEntity(ResenaCreateDTO dto, org.iesalixar.daw2.lectoraton.entities.Usuario usuario, org.iesalixar.daw2.lectoraton.entities.Libro libro) {
        Resena resena = new Resena();
        resena.setUsuario(usuario);
        resena.setLibro(libro);
        resena.setTitulo(dto.getTitulo());
        resena.setContenido(dto.getContenido());
        resena.setContieneSpoiler(Boolean.TRUE.equals(dto.getContieneSpoiler()));
        resena.setCalificacion(dto.getCalificacion());
        return resena;
    }
}
