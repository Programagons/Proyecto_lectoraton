package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.EstadoLectura;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibro;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibroId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioLibroRepository extends JpaRepository<UsuarioLibro, UsuarioLibroId> {

    long countByLibroIdAndCalificacionIsNotNull(Long libroId);

    @Query("SELECT AVG(ul.calificacion) FROM UsuarioLibro ul WHERE ul.libro.id = :libroId AND ul.calificacion IS NOT NULL")
    Double averageCalificacionByLibroId(Long libroId);

    List<UsuarioLibro> findByUsuarioIdAndLibroIdIn(Long usuarioId, List<Long> libroIds);

    Optional<UsuarioLibro> findByUsuarioIdAndLibroId(Long usuarioId, Long libroId);

    List<UsuarioLibro> findByLibroIdAndUsuarioIdInAndEstado(Long libroId, List<Long> usuarioIds, EstadoLectura estado);

    @Query("SELECT ul FROM UsuarioLibro ul JOIN FETCH ul.libro WHERE ul.usuario.id = :uid AND ul.fechaActualizacion IS NOT NULL ORDER BY ul.fechaActualizacion DESC")
    List<UsuarioLibro> findUltimosPorFechaActualizacion(@Param("uid") Long usuarioId, Pageable pageable);
}
