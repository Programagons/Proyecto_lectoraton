package org.iesalixar.daw2.lectoraton.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.TropoCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.TropoDTO;
import org.iesalixar.daw2.lectoraton.entities.Tropo;
import org.iesalixar.daw2.lectoraton.mappers.TropoMapper;
import org.iesalixar.daw2.lectoraton.repositories.TropoRepository;
import org.iesalixar.daw2.lectoraton.services.TropoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador REST que maneja las operaciones CRUD para la entidad Tropo.
 * Expone endpoints para gestionar tropos mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/api/tropos")
public class TropoController {

    private static final Logger logger = LoggerFactory.getLogger(TropoController.class);

    @Autowired
    private TropoService tropoService;

    @Autowired
    private TropoMapper tropoMapper;

    @Autowired
    private TropoRepository tropoRepository;

    /**
     * Lista todos los tropos almacenados en la base de datos.
     *
     * @return ResponseEntity con la lista de tropos o error en caso de fallo.
     */
    @Operation(summary = "Obtener todos los tropos", description = "Devuelve una lista de todos los tropos disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tropos recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TropoDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping()
    public ResponseEntity<List<TropoDTO>> getAllTropos() {
        logger.info("Solicitando la lista de todos los tropos.");
        try {
            List<TropoDTO> tropos = tropoService.getAllTropos();
            logger.info("Se han encontrado {} tropos.", tropos.size());
            return ResponseEntity.ok(tropos);
        } catch (Exception e) {
            logger.error("Error al listar los tropos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Muestra el formulario para crear un nuevo tropo.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo tropo.");
        model.addAttribute("tropo", new Tropo());
        return "tropo-form";
    }

    /**
     * Muestra el formulario para editar un tropo existente.
     *
     * @param id    ID del tropo a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para el tropo con ID {}", id);
        Optional<Tropo> tropoOpt = tropoRepository.findById(id);
        if (!tropoOpt.isPresent()) {
            logger.warn("No se encontró el tropo con ID {}", id);
        }
        model.addAttribute("tropo", tropoOpt.orElse(new Tropo()));
        return "tropo-form";
    }

    /**
     * Obtiene un tropo específico por su ID.
     *
     * @param id ID del tropo solicitado.
     * @return ResponseEntity con el tropo encontrado o mensaje de error si no existe.
     */
    @Operation(summary = "Obtener un tropo por ID", description = "Recupera un tropo específico según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tropo encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TropoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tropo no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getTropoById(@PathVariable Long id) {
        logger.info("Buscando tropo con ID {}", id);
        try {
            Optional<TropoDTO> tropoDTO = tropoService.getTropoById(id);
            if (tropoDTO.isPresent()) {
                logger.info("Tropo con ID {} encontrado", id);
                return ResponseEntity.ok(tropoDTO.get());
            } else {
                logger.warn("No se encontró ningún tropo con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El tropo no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar el tropo con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el tropo.");
        }
    }

    /**
     * Crea un nuevo tropo en la base de datos.
     *
     * @param dto DTO con los datos del nuevo tropo.
     * @return ResponseEntity con el tropo creado o mensaje de error.
     */
    @Operation(summary = "Crear un nuevo tropo", description = "Permite registrar un nuevo tropo en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tropo creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TropoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping()
    public ResponseEntity<?> createTropo(@Valid @RequestBody TropoCreateDTO dto) {
        logger.info("Insertando nuevo tropo con nombre {}", dto.getNombre());
        try {
            TropoDTO createdTropo = tropoService.createTropo(dto, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTropo);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear el tropo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear el tropo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el tropo.");
        }
    }

    /**
     * Actualiza un tropo existente por su ID.
     *
     * @param id     ID del tropo a actualizar.
     * @param dto    DTO con los datos para actualizar el tropo.
     * @param locale Idioma para mensajes de error.
     * @return ResponseEntity con el tropo actualizado o mensaje de error.
     */
    @Operation(summary = "Actualizar un tropo", description = "Permite actualizar un tropo en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tropo actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TropoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTropo(@PathVariable Long id, @Valid @RequestBody TropoCreateDTO dto, Locale locale) {
        logger.info("Actualizando tropo con ID {}", id);
        try {
            TropoDTO updatedTropo = tropoService.updateTropo(id, dto, locale);
            return ResponseEntity.ok(updatedTropo);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al actualizar el tropo con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar el tropo con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el tropo.");
        }
    }

    /**
     * Elimina un tropo específico por su ID.
     *
     * @param id ID del tropo a eliminar.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @Operation(summary = "Eliminar un tropo", description = "Permite eliminar un tropo específico en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tropo eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tropo no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTropo(@PathVariable Long id) {
        logger.info("Eliminando tropo con ID {}", id);
        try {
            tropoService.deleteTropo(id);
            return ResponseEntity.ok("Tropo eliminado con éxito.");
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar el tropo con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar el tropo con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el tropo.");
        }
    }
}
