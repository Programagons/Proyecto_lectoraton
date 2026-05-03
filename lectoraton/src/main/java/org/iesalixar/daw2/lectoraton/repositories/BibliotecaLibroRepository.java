package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.BibliotecaLibro;
import org.iesalixar.daw2.lectoraton.entities.BibliotecaLibroId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BibliotecaLibroRepository extends JpaRepository<BibliotecaLibro, BibliotecaLibroId> {

    boolean existsByBibliotecaUsuarioIdAndLibroId(Long usuarioId, Long libroId);

    boolean existsByBibliotecaIdAndLibroId(Long bibliotecaId, Long libroId);

    List<BibliotecaLibro> findByBibliotecaId(Long bibliotecaId);
}
