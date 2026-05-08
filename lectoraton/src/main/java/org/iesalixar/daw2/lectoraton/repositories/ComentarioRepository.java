package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByResenaIdOrderByFechaCreacionAsc(Long resenaId);

    // Para buscar comentarios de un usuario en una reseña (para notis)
    List<Comentario> findByResenaUsuarioIdAndUsuarioIdNotOrderByFechaCreacionDesc(Long usuarioId, Long actorExcluidoId);
}
