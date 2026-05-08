package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    Page<Resena> findByLibroId(Long libroId, Pageable pageable);

    Page<Resena> findByUsuarioId(Long usuarioId, Pageable pageable);

    // Para buscar en un libro por titulo o contenido
    @Query("""
            SELECT r FROM Resena r
            WHERE r.libro.id = :libroId
              AND (:texto IS NULL OR :texto = '' OR LOWER(COALESCE(r.titulo, '')) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(COALESCE(r.contenido, '')) LIKE LOWER(CONCAT('%', :texto, '%')))
            """)
    Page<Resena> buscarEnLibro(Long libroId, String texto, Pageable pageable);

    List<Resena> findByLibroIdOrderByFechaCreacionDesc(Long libroId, Pageable pageable);

    boolean existsByUsuarioIdAndLibroId(Long usuarioId, Long libroId);

    Optional<Resena> findByUsuarioIdAndLibroId(Long usuarioId, Long libroId);

    long countByLibroId(Long libroId);

    List<Resena> findByUsuarioIdAndUsuariosQueDieronLikeIdNot(Long usuarioId, Long actorExcluidoId);
}
