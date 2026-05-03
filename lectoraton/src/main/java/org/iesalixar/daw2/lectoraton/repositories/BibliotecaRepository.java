package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.Biblioteca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BibliotecaRepository extends JpaRepository<Biblioteca, Long> {

    List<Biblioteca> findByUsuarioIdOrderByNombreAsc(Long usuarioId);

    Optional<Biblioteca> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndNombreIgnoreCase(Long usuarioId, String nombre);

    boolean existsByUsuarioIdAndNombreIgnoreCaseAndIdNot(Long usuarioId, String nombre, Long id);
}
