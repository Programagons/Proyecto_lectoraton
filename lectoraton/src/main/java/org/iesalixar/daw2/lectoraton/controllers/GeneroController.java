package org.iesalixar.daw2.lectoraton.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.GeneroCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.GeneroDTO;
import org.iesalixar.daw2.lectoraton.entities.Genero;
import org.iesalixar.daw2.lectoraton.mappers.GeneroMapper;
import org.iesalixar.daw2.lectoraton.repositories.GeneroRepository;
import org.iesalixar.daw2.lectoraton.services.GeneroService;
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
 * Controlador REST que maneja las operaciones CRUD para la entidad Genero.
 * Expone endpoints para gestionar géneros mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/api/generos")
public class GeneroController {

    private static final Logger logger = LoggerFactory.getLogger(GeneroController.class);

    @Autowired
    private GeneroService generoService;

    @Autowired
    private GeneroMapper generoMapper;

    @Autowired
    private GeneroRepository generoRepository;

    /**
     * Lista todos los géneros almacenados en la base de datos.
     *
     * @return ResponseEntity con la lista de géneros o error en caso de fallo.
     */
    @Operation(summary = "Obtener todos los géneros", description = "Devuelve una lista de todos los géneros disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de géneros recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GeneroDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping()
    public ResponseEntity<List<GeneroDTO>> getAllGeneros() {
        logger.info("Solicitando la lista de todos los géneros.");
        try {
            List<GeneroDTO> generos = generoService.getAllGeneros();
            logger.info("Se han encontrado {} géneros.", generos.size());
            return ResponseEntity.ok(generos);
        } catch (Exception e) {
            logger.error("Error al listar los géneros: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Muestra el formulario para crear un nuevo género.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo género.");
        model.addAttribute("genero", new Genero());
        return "genero-form";
    }

    /**
     * Muestra el formulario para editar un género existente.
     *
     * @param id    ID del género a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para el género con ID {}", id);
        Optional<Genero> generoOpt = generoRepository.findById(id);
        if (!generoOpt.isPresent()) {
            logger.warn("No se encontró el género con ID {}", id);
        }
        model.addAttribute("genero", generoOpt.orElse(new Genero()));
        return "genero-form";
    }

    /**
     * Obtiene un género específico por su ID.
     *
     * @param id ID del género solicitado.
     * @return ResponseEntity con el género encontrado o mensaje de error si no existe.
     */
    @Operation(summary = "Obtener un género por ID", description = "Recupera un género específico según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Género encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GeneroDTO.class))),
            @ApiResponse(responseCode = "404", description = "Género no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getGeneroById(@PathVariable Long id) {
        logger.info("Buscando género con ID {}", id);
        try {
            Optional<GeneroDTO> generoDTO = generoService.getGeneroById(id);
            if (generoDTO.isPresent()) {
                logger.info("Género con ID {} encontrado", id);
                return ResponseEntity.ok(generoDTO.get());
            } else {
                logger.warn("No se encontró ningún género con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El género no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar el género con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el género.");
        }
    }

    /**
     * Crea un nuevo género en la base de datos.
     *
     * @param dto DTO con los datos del nuevo género.
     * @return ResponseEntity con el género creado o mensaje de error.
     */
    @Operation(summary = "Crear un nuevo género", description = "Permite registrar un nuevo género en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Género creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GeneroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping()
    public ResponseEntity<?> createGenero(@Valid @RequestBody GeneroCreateDTO dto) {
        logger.info("Insertando nuevo género con nombre {}", dto.getNombre());
        try {
            GeneroDTO createdGenero = generoService.createGenero(dto, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGenero);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear el género: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear el género: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el género.");
        }
    }

    /**
     * Actualiza un género existente por su ID.
     *
     * @param id    ID del género a actualizar.
     * @param dto   DTO con los datos para actualizar el género.
     * @param locale Idioma para mensajes de error.
     * @return ResponseEntity con el género actualizado o mensaje de error.
     */
    @Operation(summary = "Actualizar un género", description = "Permite actualizar un género en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Género actualizado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GeneroDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGenero(@PathVariable Long id, @Valid @RequestBody GeneroCreateDTO dto, Locale locale) {
        logger.info("Actualizando género con ID {}", id);
        try {
            GeneroDTO updatedGenero = generoService.updateGenero(id, dto, locale);
            return ResponseEntity.ok(updatedGenero);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al actualizar el género con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar el género con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el género.");
        }
    }

    /**
     * Elimina un género específico por su ID.
     *
     * @param id ID del género a eliminar.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @Operation(summary = "Eliminar un género", description = "Permite eliminar un género específico en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Género eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Género no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGenero(@PathVariable Long id) {
        logger.info("Eliminando género con ID {}", id);
        try {
            generoService.deleteGenero(id);
            return ResponseEntity.ok("Género eliminado con éxito.");
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar el género con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar el género con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el género.");
        }
    }
}
