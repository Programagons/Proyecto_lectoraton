package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.Tropo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TropoRepository extends JpaRepository<Tropo, Long> {

    List<Tropo> findAllByOrderByNombreAsc();

    Optional<Tropo> findByNombreIgnoreCase(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);
}
