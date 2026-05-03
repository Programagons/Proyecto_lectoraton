package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.ActividadUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ActividadUsuarioRepository extends JpaRepository<ActividadUsuario, Long> {

    @EntityGraph(attributePaths = {"usuarioActor", "libro"})
    Page<ActividadUsuario> findByUsuarioActor_IdInOrderByFechaCreacionDesc(Collection<Long> actorIds, Pageable pageable);
}
