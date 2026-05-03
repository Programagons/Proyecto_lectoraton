package org.iesalixar.daw2.lectoraton.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.LibroCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroDetalleDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroRecomendacionDTO;
import org.iesalixar.daw2.lectoraton.dtos.ProgresoLecturaDTO;
import org.iesalixar.daw2.lectoraton.dtos.ProgresoLecturaUpdateDTO;
import org.iesalixar.daw2.lectoraton.dtos.ResenaDTO;
import org.iesalixar.daw2.lectoraton.dtos.UltimoProgresoLibroDTO;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.mappers.LibroMapper;
import org.iesalixar.daw2.lectoraton.repositories.LibroRepository;
import org.iesalixar.daw2.lectoraton.services.LibroDetalleService;
import org.iesalixar.daw2.lectoraton.services.LibroRecomendacionService;
import org.iesalixar.daw2.lectoraton.services.LibroService;
import org.iesalixar.daw2.lectoraton.services.ResenaService;
import org.iesalixar.daw2.lectoraton.services.UsuarioLibroProgresoService;
import org.iesalixar.daw2.lectoraton.services.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador REST que maneja las operaciones CRUD para la entidad Libro.
 * Expone endpoints para gestionar libros mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private static final Logger logger = LoggerFactory.getLogger(LibroController.class);

    @Autowired
    private LibroService libroService;

    @Autowired
    private LibroMapper libroMapper;

    @Autowired
    private LibroRepository libroRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private LibroDetalleService libroDetalleService;
    @Autowired
    private LibroRecomendacionService libroRecomendacionService;
    @Autowired
    private ResenaService resenaService;
    @Autowired
    private UsuarioLibroProgresoService usuarioLibroProgresoService;

    /**
     * Lista todos los libros almacenados en la base de datos con paginación.
     *
     * @param pageable Parámetros de paginación y ordenación.
     * @return ResponseEntity con la página de libros o error en caso de fallo.
     */
    @Operation(summary = "Obtener todos los libros", description = "Devuelve una lista paginada de todos los libros disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de libros recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LibroDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping()
    public ResponseEntity<Page<LibroDTO>> getAllLibros(@PageableDefault(size = 10, sort = "titulo") Pageable pageable) {
        logger.info("Solicitando la lista de todos los libros con paginación: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<LibroDTO> libros = libroService.getAllLibros(pageable);
            logger.info("Se han encontrado {} libros.", libros.getTotalElements());
            return ResponseEntity.ok(libros);
        } catch (Exception e) {
            logger.error("Error al listar los libros: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Muestra el formulario para crear un nuevo libro.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo libro.");
        model.addAttribute("libro", new Libro());
        return "libro-form";
    }

    /**
     * Muestra el formulario para editar un libro existente.
     *
     * @param id    ID del libro a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para el libro con ID {}", id);
        Optional<Libro> libroOpt = libroRepository.findById(id);
        if (!libroOpt.isPresent()) {
            logger.warn("No se encontró el libro con ID {}", id);
        }
        model.addAttribute("libro", libroOpt.orElse(new Libro()));
        return "libro-form";
    }

    @GetMapping("/mi/ultimo-progreso")
    public ResponseEntity<UltimoProgresoLibroDTO> getMiUltimoProgreso(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        UltimoProgresoLibroDTO dto = usuarioLibroProgresoService.getUltimoActualizado(usuarioId);
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Obtiene un libro específico por su ID.
     *
     * @param id ID del libro solicitado.
     * @return ResponseEntity con el libro encontrado o mensaje de error si no existe.
     */
    @Operation(summary = "Obtener un libro por ID", description = "Recupera un libro específico según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LibroDTO.class))),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getLibroById(@PathVariable Long id) {
        logger.info("Buscando libro con ID {}", id);
        try {
            Optional<LibroDTO> libroDTO = libroService.getLibroById(id);
            if (libroDTO.isPresent()) {
                logger.info("Libro con ID {} encontrado", id);
                return ResponseEntity.ok(libroDTO.get());
            } else {
                logger.warn("No se encontró ningún libro con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El libro no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar el libro con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el libro.");
        }
    }

    @GetMapping("/{id}/recomendacion")
    public ResponseEntity<LibroRecomendacionDTO> getRecomendacion(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(libroRecomendacionService.recomendar(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<?> getLibroDetalle(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            LibroDetalleDTO dto = libroDetalleService.getDetalle(id, usuarioId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/progreso")
    public ResponseEntity<?> putProgresoLectura(@PathVariable Long id,
                                                @Valid @RequestBody ProgresoLecturaUpdateDTO body,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            ProgresoLecturaDTO dto = usuarioLibroProgresoService.actualizar(id, usuarioId, body);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/resenas")
    public ResponseEntity<Page<ResenaDTO>> getResenasLibroConBusqueda(@PathVariable Long id,
                                                                       @RequestParam(value = "q", required = false) String q,
                                                                       @AuthenticationPrincipal UserDetails userDetails,
                                                                       @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {
        Long usuarioId = null;
        if (userDetails != null) {
            usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        }
        return ResponseEntity.ok(resenaService.buscarEnLibro(id, q, pageable, usuarioId));
    }

    @PostMapping("/resenas/{resenaId}/like")
    public ResponseEntity<?> toggleLikeResena(@PathVariable Long resenaId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            return ResponseEntity.ok(resenaService.toggleLike(resenaId, usuarioId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/explorar")
    public ResponseEntity<Page<LibroDTO>> explorarLibros(@RequestParam(value = "titulo", required = false) String titulo,
                                                         @RequestParam(value = "autor", required = false) String autor,
                                                         @RequestParam(value = "autorId", required = false) Long autorId,
                                                         @RequestParam(value = "generoId", required = false) Long generoId,
                                                         @RequestParam(value = "tropoId", required = false) Long tropoId,
                                                         @RequestParam(value = "saga", required = false) String saga,
                                                         @PageableDefault(size = 24, sort = "titulo") Pageable pageable) {
        return ResponseEntity.ok(libroService.explorar(titulo, autor, autorId, generoId, tropoId, saga, pageable));
    }

    @GetMapping("/novedades")
    public ResponseEntity<List<LibroDTO>> novedades(@RequestParam(value = "size", defaultValue = "12") int size) {
        return ResponseEntity.ok(libroService.listarNovedades(size));
    }

    @GetMapping("/mas-leidos")
    public ResponseEntity<List<LibroDTO>> masLeidos(@RequestParam(value = "size", defaultValue = "12") int size) {
        return ResponseEntity.ok(libroService.listarMasLeidosPorCalificaciones(size));
    }

    /**
     * Crea un nuevo libro en la base de datos.
     *
     * @param dto DTO con los datos del nuevo libro.
     * @return ResponseEntity con el libro creado o mensaje de error.
     */
    @Operation(summary = "Crear un nuevo libro", description = "Permite registrar un nuevo libro en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Libro creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LibroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o ISBN duplicado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('Editor') or hasAuthority('Admin')")
    public ResponseEntity<?> createLibro(@Valid @ModelAttribute LibroCreateDTO dto) {
        logger.info("Insertando nuevo libro con título {}", dto.getTitulo());
        try {
            LibroDTO createdLibro = libroService.createLibro(dto, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLibro);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear el libro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error al guardar la portada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la portada.");
        } catch (Exception e) {
            logger.error("Error al crear el libro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el libro.");
        }
    }

    /**
     * Actualiza un libro existente por su ID.
     *
     * @param id   ID del libro a actualizar.
     * @param dto  DTO con los datos para actualizar el libro.
     * @param locale Idioma para mensajes de error.
     * @return ResponseEntity con el libro actualizado o mensaje de error.
     */
    @Operation(summary = "Actualizar un libro", description = "Permite actualizar un libro en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LibroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateLibro(@PathVariable Long id, @Valid @ModelAttribute LibroCreateDTO dto, Locale locale) {
        logger.info("Actualizando libro con ID {}", id);
        try {
            LibroDTO updatedLibro = libroService.updateLibro(id, dto, locale);
            return ResponseEntity.ok(updatedLibro);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al actualizar el libro con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error al guardar la portada para el libro con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la portada.");
        } catch (Exception e) {
            logger.error("Error al actualizar el libro con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el libro.");
        }
    }

    /**
     * Elimina un libro específico por su ID.
     *
     * @param id ID del libro a eliminar.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @Operation(summary = "Eliminar un libro", description = "Permite eliminar un libro específico en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLibro(@PathVariable Long id) {
        logger.info("Eliminando libro con ID {}", id);
        try {
            libroService.deleteLibro(id);
            return ResponseEntity.ok("Libro eliminado con éxito.");
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar el libro con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar el libro con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el libro.");
        }
    }

    private Sort getSort(String sort) {
        if (sort == null) {
            return Sort.by("id").ascending();
        }
        return switch (sort) {
            case "tituloAsc" -> Sort.by("titulo").ascending();
            case "tituloDesc" -> Sort.by("titulo").descending();
            case "idDesc" -> Sort.by("id").descending();
            default -> Sort.by("id").ascending();
        };
    }
}
