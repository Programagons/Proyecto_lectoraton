package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.NotificacionDTO;
import org.iesalixar.daw2.lectoraton.entities.Comentario;
import org.iesalixar.daw2.lectoraton.entities.Resena;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.repositories.ComentarioRepository;
import org.iesalixar.daw2.lectoraton.repositories.ResenaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class NotificacionService {

    private final ComentarioRepository comentarioRepository;
    private final ResenaRepository resenaRepository;

    public NotificacionService(ComentarioRepository comentarioRepository,
                               ResenaRepository resenaRepository) {
        this.comentarioRepository = comentarioRepository;
        this.resenaRepository = resenaRepository;
    }

    public List<NotificacionDTO> getMisNotificaciones(Long usuarioId) {
        List<NotificacionDTO> notificaciones = new ArrayList<>();

        List<Comentario> comentarios = comentarioRepository
                .findByResenaUsuarioIdAndUsuarioIdNotOrderByFechaCreacionDesc(usuarioId, usuarioId);
        for (Comentario comentario : comentarios) {
            NotificacionDTO dto = new NotificacionDTO();
            dto.setTipo("comentario");
            dto.setActorNombre(nombreCompleto(comentario.getUsuario()));
            dto.setLibroTitulo(comentario.getResena().getLibro().getTitulo());
            dto.setResenaTitulo(comentario.getResena().getTitulo());
            dto.setFecha(comentario.getFechaCreacion());
            dto.setMensaje(dto.getActorNombre() + " comentó en tu reseña de " + dto.getLibroTitulo() + ".");
            notificaciones.add(dto);
        }

        List<Resena> resenasConLikes = resenaRepository.findByUsuarioIdAndUsuariosQueDieronLikeIdNot(usuarioId, usuarioId);
        for (Resena resena : resenasConLikes) {
            for (Usuario usuarioLike : resena.getUsuariosQueDieronLike()) {
                if (usuarioLike.getId().equals(usuarioId)) {
                    continue;
                }
                NotificacionDTO dto = new NotificacionDTO();
                dto.setTipo("like");
                dto.setActorNombre(nombreCompleto(usuarioLike));
                dto.setLibroTitulo(resena.getLibro().getTitulo());
                dto.setResenaTitulo(resena.getTitulo());
                dto.setFecha(null);
                dto.setMensaje(dto.getActorNombre() + " le dio me gusta a tu reseña de " + dto.getLibroTitulo() + ".");
                notificaciones.add(dto);
            }
        }

        notificaciones.sort(Comparator.comparing(NotificacionDTO::getFecha,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return notificaciones;
    }

    private String nombreCompleto(Usuario usuario) {
        return usuario.getNombre() + " " + usuario.getApellidos();
    }
}
