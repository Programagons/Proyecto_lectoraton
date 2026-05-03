package org.iesalixar.daw2.lectoraton.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.AutorCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.AutorDTO;
import org.iesalixar.daw2.lectoraton.entities.Autor;
import org.iesalixar.daw2.lectoraton.mappers.AutorMapper;
import org.iesalixar.daw2.lectoraton.repositories.AutorRepository;
import org.iesalixar.daw2.lectoraton.services.AutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;

/**
 * Controlador REST que maneja las operaciones CRUD para la entidad `Autor`.
 * Expone endpoints para gestionar autores mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private static final Logger logger = LoggerFactory.getLogger(AutorController.class);

    @Autowired
    private AutorService autorService;

    @Autowired
    private AutorMapper autorMapper;

    @Autowired
    private AutorRepository autorRepository;

    /**
     *
     * Lista todos los autores almacenadas en la base de datos.
     *
     * @return ResponseEntity con la lista de autores o un error en caso de fallo.
     */
    @Operation(summary = "Obtener todos los autores", description = "Devuelve una lista de todos los autores disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de autores recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AutorDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping()
    public ResponseEntity<Page<AutorDTO>> getAllAutores(@PageableDefault(size = 10, sort = "nombreCompleto") Pageable pageable){
        logger.info("Solicitando la lista de todos los autores con paginación: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<AutorDTO> autores = autorService.getAllAutores(pageable);
            logger.info("Se han encontrado {} autores.", autores.getTotalElements());
            return ResponseEntity.ok(autores);
        } catch (Exception e) {
            logger.error("Error al listar los autores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    /**
     * Obtiene un autor específico por su ID.
     *
     * @param id ID del autor solicitado.
     * @return ResponseEntity con el autor encontrado o un mensaje de error si no existe.
     */
    @Operation(summary = "Obtener un autor por ID", description = "Recupera un autor especifico según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AutorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getAutorById(@PathVariable Long id) {
        logger.info("Buscando autor con ID {}", id);
        try {
            Optional<AutorDTO> autorDTO = autorService.getAutorById(id);
            if (autorDTO.isPresent()) {
                logger.info("Autor con ID {} encontrado", id);
                return ResponseEntity.ok(autorDTO.get());
            } else {
                logger.warn("No se encontró ningún autor con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El autor no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar el autor con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el autor.");
        }
    }

    /**
     * Muestra el formulario para crear un nuevo autor.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return El nombre de la plantilla Thymeleaf para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo autor.");
        model.addAttribute("autor", new Autor()); // Crear un nuevo objeto Autor
        return "autor-form"; // Nombre de la plantilla Thymeleaf para el formulario
    }

    /**
     * Muestra el formulario para editar un autor existente.
     *
     * @param id    ID del autor a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return El nombre de la plantilla Thymeleaf para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para el autor con ID {}", id);
        Optional<Autor> autorOpt = autorRepository.findById(id);
        if (!autorOpt.isPresent()) {
            logger.warn("No se encontró el autor con ID {}", id);
        }
        model.addAttribute("autor", autorOpt.get());
        return "autor-form"; // Nombre de la plantilla Thymeleaf para el formulario
    }

    /**
     * Crea un nuevo autor en la base de datos.
     *
     * @param autorCreateDTO Objeto JSON que representa el nuevo autor.
     * @return ResponseEntity con el autor creado o un mensaje de error.
     */
    @Operation(summary = "Crear un nuevo autor", description = "Permite registrar un nuevo autor en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Autor creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AutorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos proporcionados"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createAutor(@Valid @ModelAttribute AutorCreateDTO autorCreateDTO){
        logger.info("Insertando nuevo autor con nombre {}", autorCreateDTO.getNombreCompleto());
        try {
            AutorDTO createdAutor = autorService.createAutor(autorCreateDTO, Locale.FRENCH);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAutor);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear el autor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error al guardar la imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la imagen.");
        } catch(Exception e) {
            logger.error("Error al crear el autor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el autor.");
        }
    }

    /**
     * Actualiza un autor existente por su ID.
     *
     * @param id ID del autor a actualizar.
     * @param autorCreateDTO DTO con los datos para actualizar el autor.
     * @param locale Idioma de los mensajes de error.
     * @return ResponseEntity con el autor actualizado o un mensaje de error.
     *
     */
    @Operation(summary = "Actualizar un autor", description = "Permite actualizar un autor en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Autor actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AutorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateAutor (@PathVariable Long id, @Valid @ModelAttribute AutorCreateDTO autorCreateDTO, Locale locale){
        logger.info("Actualizando autor con ID {}", id);
        try {
            AutorDTO updatedAutor = autorService.updateAutor(id, autorCreateDTO, locale);
            return ResponseEntity.ok(updatedAutor);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al actualizar el autor con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e){
            logger.error("Error al guardar la imagen para el autor con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la imagen.");
        } catch (Exception e) {
            logger.error("Error al actualizar el autor con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el autor.");
        }
    }


    /**
     * Elimina un autor específico por su ID.
     * @param id ID del autor a eliminar.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @Operation(summary = "Eliminar un autor", description = "Permite eliminar un autor específico en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAutor(@PathVariable Long id){
        logger.info("Eliminando autor con ID {}", id);
        try {
            autorService.deleteAutor(id);
            return ResponseEntity.ok("Autor eliminado con éxito.");
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar el autor con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar el autor con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el autor.");
        }
    }

    private Sort getSort(String sort) {
        if (sort == null) {
            return Sort.by("id").ascending();
        }
        return switch (sort) {
            case "nombreAsc" -> Sort.by("nombreCompleto").ascending();
            case "nombreDesc" -> Sort.by("nombreCompleto").descending();
            case "idDesc" -> Sort.by("id").descending();
            default -> Sort.by("id").ascending();
        };
    }
}
