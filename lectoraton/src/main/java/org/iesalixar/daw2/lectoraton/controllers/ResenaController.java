package org.iesalixar.daw2.lectoraton.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.CalificacionCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.ResenaCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.ResenaDTO;
import org.iesalixar.daw2.lectoraton.dtos.ResenaUpdateDTO;
import org.iesalixar.daw2.lectoraton.entities.Resena;
import org.iesalixar.daw2.lectoraton.mappers.ResenaMapper;
import org.iesalixar.daw2.lectoraton.repositories.ResenaRepository;
import org.iesalixar.daw2.lectoraton.services.ResenaService;
import org.iesalixar.daw2.lectoraton.services.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST que maneja las operaciones CRUD para la entidad Resena.
 * Expone endpoints para gestionar reseñas mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private static final Logger logger = LoggerFactory.getLogger(ResenaController.class);

    @Autowired
    private ResenaService resenaService;

    @Autowired
    private ResenaMapper resenaMapper;

    @Autowired
    private ResenaRepository resenaRepository;
    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista las reseñas de un libro con paginación.
     *
     * @param libroId  ID del libro.
     * @param pageable Parámetros de paginación.
     * @return ResponseEntity con la página de reseñas o error en caso de fallo.
     */
    @Operation(summary = "Reseñas por libro", description = "Devuelve las reseñas de un libro con paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reseñas recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResenaDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/libro/{libroId}")
    public ResponseEntity<Page<ResenaDTO>> getResenasByLibroId(@PathVariable Long libroId,
                                                               @PageableDefault(size = 10) Pageable pageable) {
        logger.info("Solicitando reseñas del libro con ID {}", libroId);
        try {
            Page<ResenaDTO> resenas = resenaService.getByLibroId(libroId, pageable);
            logger.info("Se han encontrado {} reseñas para el libro {}", resenas.getNumberOfElements(), libroId);
            return ResponseEntity.ok(resenas);
        } catch (Exception e) {
            logger.error("Error al listar las reseñas del libro {}: {}", libroId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lista las reseñas de un usuario con paginación.
     *
     * @param usuarioId ID del usuario.
     * @param pageable  Parámetros de paginación.
     * @return ResponseEntity con la página de reseñas o error en caso de fallo.
     */
    @Operation(summary = "Reseñas por usuario", description = "Devuelve las reseñas de un usuario con paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reseñas recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResenaDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<ResenaDTO>> getResenasByUsuarioId(@PathVariable Long usuarioId,
                                                                 @PageableDefault(size = 10) Pageable pageable) {
        logger.info("Solicitando reseñas del usuario con ID {}", usuarioId);
        try {
            Page<ResenaDTO> resenas = resenaService.getByUsuarioId(usuarioId, pageable);
            logger.info("Se han encontrado {} reseñas para el usuario {}", resenas.getNumberOfElements(), usuarioId);
            return ResponseEntity.ok(resenas);
        } catch (Exception e) {
            logger.error("Error al listar las reseñas del usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/libro/{libroId}/mia")
    public ResponseEntity<?> getMiResenaEnLibro(@PathVariable Long libroId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        return resenaService.getMiResenaEnLibro(usuarioId, libroId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tienes reseña en este libro."));
    }

    /**
     * Muestra el formulario para crear una nueva reseña.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva reseña.");
        model.addAttribute("resena", new Resena());
        return "resena-form";
    }

    /**
     * Muestra el formulario para editar una reseña existente.
     *
     * @param id    ID de la reseña a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para la reseña con ID {}", id);
        Optional<Resena> resenaOpt = resenaRepository.findById(id);
        if (!resenaOpt.isPresent()) {
            logger.warn("No se encontró la reseña con ID {}", id);
        }
        model.addAttribute("resena", resenaOpt.orElse(new Resena()));
        return "resena-form";
    }

    /**
     * Obtiene una reseña específica por su ID.
     *
     * @param id ID de la reseña solicitada.
     * @return ResponseEntity con la reseña encontrada o mensaje de error si no existe.
     */
    @Operation(summary = "Obtener una reseña por ID", description = "Recupera una reseña específica según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getResenaById(@PathVariable Long id) {
        logger.info("Buscando reseña con ID {}", id);
        try {
            Optional<ResenaDTO> resenaDTO = resenaService.getById(id);
            if (resenaDTO.isPresent()) {
                logger.info("Reseña con ID {} encontrada", id);
                return ResponseEntity.ok(resenaDTO.get());
            } else {
                logger.warn("No se encontró ninguna reseña con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La reseña no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar la reseña con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar la reseña.");
        }
    }

    /**
     * Crea una nueva reseña en la base de datos.
     *
     * @param dto DTO con los datos de la nueva reseña.
     * @return ResponseEntity con la reseña creada o mensaje de error.
     */
    @Operation(summary = "Crear una nueva reseña", description = "Permite registrar una nueva reseña en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reseña creada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResenaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o reseña duplicada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping()
    public ResponseEntity<?> createResena(@Valid @RequestBody ResenaCreateDTO dto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        dto.setUsuarioId(usuarioService.getIdByUsername(userDetails.getUsername()));
        logger.info("Insertando nueva reseña para libro {} y usuario {}", dto.getLibroId(), dto.getUsuarioId());
        try {
            ResenaDTO createdResena = resenaService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdResena);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear la reseña: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear la reseña: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear la reseña.");
        }
    }

    @PostMapping("/libro/{libroId}/calificacion")
    public ResponseEntity<?> calificarLibro(@PathVariable Long libroId,
                                            @Valid @RequestBody CalificacionCreateDTO dto,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            resenaService.calificarLibro(usuarioId, libroId, dto.getCalificacion());
            return ResponseEntity.ok("Calificación guardada.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Elimina una reseña específica por su ID.
     *
     * @param id ID de la reseña a eliminar.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @Operation(summary = "Eliminar una reseña", description = "Permite eliminar una reseña específica en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResena(@PathVariable Long id) {
        logger.info("Eliminando reseña con ID {}", id);
        try {
            resenaService.delete(id);
            return ResponseEntity.ok("Reseña eliminada con éxito.");
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar la reseña con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar la reseña con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la reseña.");
        }
    }

    @DeleteMapping("/{id}/mia")
    public ResponseEntity<?> deleteMiResena(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            resenaService.deletePropia(id, usuarioId);
            return ResponseEntity.ok("Reseña eliminada con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/mia")
    public ResponseEntity<?> updateMiResena(@PathVariable Long id,
                                            @Valid @RequestBody ResenaUpdateDTO dto,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            return ResponseEntity.ok(resenaService.updatePropia(id, usuarioId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
