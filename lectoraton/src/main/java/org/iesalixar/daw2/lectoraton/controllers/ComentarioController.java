package org.iesalixar.daw2.lectoraton.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.ComentarioCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.ComentarioDTO;
import org.iesalixar.daw2.lectoraton.entities.Comentario;
import org.iesalixar.daw2.lectoraton.mappers.ComentarioMapper;
import org.iesalixar.daw2.lectoraton.repositories.ComentarioRepository;
import org.iesalixar.daw2.lectoraton.services.ComentarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST que maneja las operaciones CRUD para la entidad Comentario.
 * Expone endpoints para gestionar comentarios en reseñas mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private static final Logger logger = LoggerFactory.getLogger(ComentarioController.class);

    @Autowired
    private ComentarioService comentarioService;

    @Autowired
    private ComentarioMapper comentarioMapper;

    @Autowired
    private ComentarioRepository comentarioRepository;

    /**
     * Lista los comentarios de una reseña.
     *
     * @param resenaId ID de la reseña.
     * @return ResponseEntity con la lista de comentarios o error en caso de fallo.
     */
    @Operation(summary = "Comentarios de una reseña", description = "Devuelve todos los comentarios de una reseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de comentarios recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ComentarioDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/resena/{resenaId}")
    public ResponseEntity<List<ComentarioDTO>> getComentariosByResenaId(@PathVariable Long resenaId) {
        logger.info("Solicitando comentarios de la reseña con ID {}", resenaId);
        try {
            List<ComentarioDTO> comentarios = comentarioService.getByResenaId(resenaId);
            logger.info("Se han encontrado {} comentarios para la reseña {}", comentarios.size(), resenaId);
            return ResponseEntity.ok(comentarios);
        } catch (Exception e) {
            logger.error("Error al listar los comentarios de la reseña {}: {}", resenaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Muestra el formulario para crear un nuevo comentario.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo comentario.");
        model.addAttribute("comentario", new Comentario());
        return "comentario-form";
    }

    /**
     * Muestra el formulario para editar un comentario existente.
     *
     * @param id    ID del comentario a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para el comentario con ID {}", id);
        Optional<Comentario> comentarioOpt = comentarioRepository.findById(id);
        if (!comentarioOpt.isPresent()) {
            logger.warn("No se encontró el comentario con ID {}", id);
        }
        model.addAttribute("comentario", comentarioOpt.orElse(new Comentario()));
        return "comentario-form";
    }

    /**
     * Obtiene un comentario específico por su ID.
     *
     * @param id ID del comentario solicitado.
     * @return ResponseEntity con el comentario encontrado o mensaje de error si no existe.
     */
    @Operation(summary = "Obtener un comentario por ID", description = "Recupera un comentario específico según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentario encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ComentarioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Comentario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getComentarioById(@PathVariable Long id) {
        logger.info("Buscando comentario con ID {}", id);
        try {
            Optional<ComentarioDTO> comentarioDTO = comentarioService.getById(id);
            if (comentarioDTO.isPresent()) {
                logger.info("Comentario con ID {} encontrado", id);
                return ResponseEntity.ok(comentarioDTO.get());
            } else {
                logger.warn("No se encontró ningún comentario con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El comentario no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar el comentario con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el comentario.");
        }
    }

    /**
     * Crea un nuevo comentario en la base de datos.
     *
     * @param dto DTO con los datos del nuevo comentario.
     * @return ResponseEntity con el comentario creado o mensaje de error.
     */
    @Operation(summary = "Crear un nuevo comentario", description = "Permite registrar un nuevo comentario en una reseña.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comentario creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ComentarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping()
    public ResponseEntity<?> createComentario(@Valid @RequestBody ComentarioCreateDTO dto,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        dto.setUsuarioId(comentarioService.getUsuarioIdAutenticado(userDetails.getUsername()));
        logger.info("Insertando nuevo comentario en reseña {} por usuario {}", dto.getResenaId(), dto.getUsuarioId());
        try {
            ComentarioDTO createdComentario = comentarioService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComentario);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear el comentario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear el comentario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el comentario.");
        }
    }

    /**
     * Elimina un comentario específico por su ID.
     *
     * @param id ID del comentario a eliminar.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @Operation(summary = "Eliminar un comentario", description = "Permite eliminar un comentario específico en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comentario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComentario(@PathVariable Long id) {
        logger.info("Eliminando comentario con ID {}", id);
        try {
            comentarioService.delete(id);
            return ResponseEntity.ok("Comentario eliminado con éxito.");
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar el comentario con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar el comentario con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el comentario.");
        }
    }
}
