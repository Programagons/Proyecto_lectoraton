package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Page<Autor> findAll(Pageable pageable);


    Page<Autor> findByNombreCompletoContainingIgnoreCase(String nombre, Pageable pageable);

    long countByNombreCompletoContainingIgnoreCase(String nombre);

    boolean existsByNombreCompleto(String nombreCompleto);

    boolean existsByNombreCompletoAndIdNot(String nombreCompleto, Long id);
}

