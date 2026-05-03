package org.iesalixar.daw2.lectoraton.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.BibliotecaCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.BibliotecaDTO;
import org.iesalixar.daw2.lectoraton.dtos.BibliotecaRenameDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroDTO;
import org.iesalixar.daw2.lectoraton.entities.Biblioteca;
import org.iesalixar.daw2.lectoraton.mappers.BibliotecaMapper;
import org.iesalixar.daw2.lectoraton.repositories.BibliotecaRepository;
import org.iesalixar.daw2.lectoraton.services.BibliotecaService;
import org.iesalixar.daw2.lectoraton.services.UsuarioService;
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
 * Controlador REST que maneja las operaciones CRUD para la entidad Biblioteca.
 * Expone endpoints para gestionar bibliotecas (listas de libros) mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/api/bibliotecas")
public class BibliotecaController {

    private static final Logger logger = LoggerFactory.getLogger(BibliotecaController.class);

    @Autowired
    private BibliotecaService bibliotecaService;

    @Autowired
    private BibliotecaMapper bibliotecaMapper;

    @Autowired
    private BibliotecaRepository bibliotecaRepository;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista las bibliotecas del usuario autenticado (JWT).
     */
    @Operation(summary = "Mis bibliotecas", description = "Devuelve las bibliotecas del usuario que envía el token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BibliotecaDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/mias")
    public ResponseEntity<List<BibliotecaDTO>> getMisBibliotecas(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        logger.info("Solicitando bibliotecas del usuario autenticado (id {})", usuarioId);
        try {
            List<BibliotecaDTO> bibliotecas = bibliotecaService.getByUsuarioId(usuarioId);
            return ResponseEntity.ok(bibliotecas);
        } catch (Exception e) {
            logger.error("Error al listar bibliotecas del usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lista las bibliotecas de un usuario.
     *
     * @param usuarioId ID del usuario.
     * @return ResponseEntity con la lista de bibliotecas o error en caso de fallo.
     */
    @Operation(summary = "Bibliotecas de un usuario", description = "Devuelve todas las bibliotecas de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de bibliotecas recuperada exitosamente",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BibliotecaDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<BibliotecaDTO>> getBibliotecasByUsuarioId(@PathVariable Long usuarioId) {
        logger.info("Solicitando bibliotecas del usuario con ID {}", usuarioId);
        try {
            List<BibliotecaDTO> bibliotecas = bibliotecaService.getByUsuarioId(usuarioId);
            logger.info("Se han encontrado {} bibliotecas para el usuario {}", bibliotecas.size(), usuarioId);
            return ResponseEntity.ok(bibliotecas);
        } catch (Exception e) {
            logger.error("Error al listar las bibliotecas del usuario {}: {}", usuarioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Muestra el formulario para crear una nueva biblioteca.
     *
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva biblioteca.");
        model.addAttribute("biblioteca", new Biblioteca());
        return "biblioteca-form";
    }

    /**
     * Muestra el formulario para editar una biblioteca existente.
     *
     * @param id    ID de la biblioteca a editar.
     * @param model Modelo para pasar datos a la vista.
     * @return Nombre de la plantilla para el formulario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para la biblioteca con ID {}", id);
        Optional<Biblioteca> bibliotecaOpt = bibliotecaRepository.findById(id);
        if (!bibliotecaOpt.isPresent()) {
            logger.warn("No se encontró la biblioteca con ID {}", id);
        }
        model.addAttribute("biblioteca", bibliotecaOpt.orElse(new Biblioteca()));
        return "biblioteca-form";
    }

    /**
     * Obtiene una biblioteca específica por su ID.
     *
     * @param id ID de la biblioteca solicitada.
     * @return ResponseEntity con la biblioteca encontrada o mensaje de error si no existe.
     */
    @Operation(summary = "Obtener una biblioteca por ID", description = "Recupera una biblioteca específica según su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biblioteca encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BibliotecaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Biblioteca no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getBibliotecaById(@PathVariable Long id) {
        logger.info("Buscando biblioteca con ID {}", id);
        try {
            Optional<BibliotecaDTO> bibliotecaDTO = bibliotecaService.getById(id);
            if (bibliotecaDTO.isPresent()) {
                logger.info("Biblioteca con ID {} encontrada", id);
                return ResponseEntity.ok(bibliotecaDTO.get());
            } else {
                logger.warn("No se encontró ninguna biblioteca con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La biblioteca no existe.");
            }
        } catch (Exception e) {
            logger.error("Error al buscar la biblioteca con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar la biblioteca.");
        }
    }

    @Operation(summary = "Libros de una biblioteca", description = "Devuelve los libros de una biblioteca del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LibroDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Biblioteca no encontrada")
    })
    @GetMapping("/{id}/libros")
    public ResponseEntity<?> getLibrosDeBiblioteca(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            List<LibroDTO> libros = bibliotecaService.getLibrosByBibliotecaIdAndUsuarioId(id, usuarioId);
            return ResponseEntity.ok(libros);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Crea una nueva biblioteca en la base de datos.
     *
     * @param dto DTO con los datos de la nueva biblioteca.
     * @return ResponseEntity con la biblioteca creada o mensaje de error.
     */
    @Operation(summary = "Crear una nueva biblioteca", description = "Permite registrar una nueva biblioteca (lista de libros) en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Biblioteca creada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BibliotecaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping()
    public ResponseEntity<?> createBiblioteca(@Valid @RequestBody BibliotecaCreateDTO dto,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        dto.setUsuarioId(usuarioService.getIdByUsername(userDetails.getUsername()));
        logger.info("Insertando nueva biblioteca con nombre {} para usuario {}", dto.getNombre(), dto.getUsuarioId());
        try {
            BibliotecaDTO createdBiblioteca = bibliotecaService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBiblioteca);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear la biblioteca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al crear la biblioteca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear la biblioteca.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> renameBiblioteca(@PathVariable Long id,
                                              @Valid @RequestBody BibliotecaRenameDTO dto,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            BibliotecaDTO updated = bibliotecaService.rename(id, usuarioId, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Elimina una biblioteca específica por su ID.
     *
     * @param id ID de la biblioteca a eliminar.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @Operation(summary = "Eliminar una biblioteca", description = "Permite eliminar una biblioteca específica en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biblioteca eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Biblioteca no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBiblioteca(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        logger.info("Eliminando biblioteca con ID {}", id);
        try {
            bibliotecaService.delete(id, usuarioId);
            return ResponseEntity.ok("Biblioteca eliminada con éxito.");
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar la biblioteca con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar la biblioteca con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la biblioteca.");
        }
    }

    @PostMapping("/{id}/libros/{libroId}")
    public ResponseEntity<?> addLibroABiblioteca(@PathVariable Long id,
                                                 @PathVariable Long libroId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        try {
            bibliotecaService.addLibro(id, usuarioId, libroId);
            return ResponseEntity.ok("Libro agregado correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
