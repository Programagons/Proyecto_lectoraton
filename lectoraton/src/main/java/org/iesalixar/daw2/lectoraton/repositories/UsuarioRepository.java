package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM Usuario u
            WHERE (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            ORDER BY u.username ASC
            """)
    List<Usuario> buscarPorTexto(String q);
}
