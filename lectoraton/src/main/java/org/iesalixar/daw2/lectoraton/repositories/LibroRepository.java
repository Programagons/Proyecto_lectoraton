package org.iesalixar.daw2.lectoraton.repositories;

import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    Page<Libro> findAll(Pageable pageable);

    Optional<Libro> findByIsbn(String isbn);

    /**
     * Obtiene un libro con sus asociaciones.
     * @param id ID del libro.
     * @return Libro.
     */
    @EntityGraph(attributePaths = {"autor", "generos", "tropos"})
    @Query("SELECT l FROM Libro l WHERE l.id = :id")
    Optional<Libro> findOneWithAssociations(@Param("id") Long id);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    Page<Libro> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

    Page<Libro> findByAutorId(Long autorId, Pageable pageable);

    /**
     * Obtiene los libros similares.
     * @param libroId ID del libro.
     * @param generoIds IDs de los géneros.
     * @param tropoIds IDs de los tropos.
     * @param pageable Parámetros de paginación.
     * @return Lista de Libro.
     */
    @Query("""
           SELECT DISTINCT l FROM Libro l
           LEFT JOIN l.generos g
           LEFT JOIN l.tropos t
           WHERE l.id <> :libroId
             AND (g.id IN :generoIds OR t.id IN :tropoIds)
           """)
    List<Libro> findSimilares(Long libroId, List<Long> generoIds, List<Long> tropoIds, Pageable pageable);

    /**
     * Obtiene los libros explorar.
     * @param titulo Titulo del libro.
     * @param autor Autor del libro.
     * @param autorId ID del autor del libro.
     * @param generoId ID del género del libro.
     * @param tropoId ID del tropo del libro.
     * @param saga Saga del libro.
     * @param pageable Parámetros de paginación.
     * @return Página de Libro.
     */
    @Query("""
           SELECT DISTINCT l FROM Libro l
           LEFT JOIN l.generos g
           LEFT JOIN l.tropos t
           WHERE (:titulo IS NULL OR :titulo = '' OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
             AND (:autor IS NULL OR :autor = '' OR LOWER(COALESCE(l.autor.nombreCompleto, '')) LIKE LOWER(CONCAT('%', :autor, '%')))
             AND (:autorId IS NULL OR l.autor.id = :autorId)
             AND (:generoId IS NULL OR g.id = :generoId)
             AND (:tropoId IS NULL OR t.id = :tropoId)
             AND (:saga IS NULL OR :saga = '' OR LOWER(COALESCE(l.sagaNombre, '')) LIKE LOWER(CONCAT('%', :saga, '%')))
           """)
    Page<Libro> explorar(@Param("titulo") String titulo,
                         @Param("autor") String autor,
                         @Param("autorId") Long autorId,
                         @Param("generoId") Long generoId,
                         @Param("tropoId") Long tropoId,
                         @Param("saga") String saga,
                         Pageable pageable);

    /**
     * Obtiene las novedades de los libros.
     * @param pageable Parámetros de paginación.
     * @return Lista de Libro.
     */
    @Query("SELECT l FROM Libro l ORDER BY l.fechaPublicacion DESC NULLS LAST, l.id DESC")
    List<Libro> findNovedades(Pageable pageable);

    /**
     * Obtiene los libros más leidos por calificaciones.
     * @param lim Limite de libros.
     * @return Lista de ID de Libro.
     */
    @Query(value = "SELECT libro_id FROM usuarios_libros WHERE calificacion IS NOT NULL GROUP BY libro_id ORDER BY COUNT(*) DESC LIMIT :lim", nativeQuery = true)
    List<Long> findLibroIdsMasCalificaciones(@Param("lim") int lim);
}
