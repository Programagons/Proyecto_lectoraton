package org.iesalixar.daw2.lectoraton.controllers;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioBioUpdateDTO;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioDTO;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioPerfilDTO;
import org.iesalixar.daw2.lectoraton.services.UsuarioService;
import org.iesalixar.daw2.lectoraton.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// Controlador REST: recibe peticiones HTTP y delega la lógica al servicio.
@RestController
@RequestMapping("/api/usuarios")
// Extraemos el id del usuario desde el JWT para operar siempre sobre el usuario autenticado.
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UserController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    // Endpoint para obtener la información del usuario logueado.
    @GetMapping("/me")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<UsuarioPerfilDTO> getUsuarioLogueado(@RequestHeader("Authorization") String tokenHeader) {
        logger.info("Solicitando la información del usuario logueado");
        String token = tokenHeader.replace("Bearer ", "").trim();
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        // Si el id es null, se devuelve un error 401.
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Se obtiene la información del usuario.
        Optional<UsuarioPerfilDTO> opt = usuarioService.getUsuarioPerfil(id);
        // Si el usuario no existe, se devuelve un error 404.
        return opt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Endpoint para solicitar el rol de editor.
    @PostMapping("/me/solicitud-editor")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<String> solicitarRolEditor(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        // Se obtiene el id del usuario desde el token.
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        // Si el id es null, se devuelve un error 401.
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Se solicita el rol de editor.
        try {
            usuarioService.solicitarRolEditor(id);
            return ResponseEntity.ok("Solicitud enviada. Revisa tu correo de administrador para aprobarla.");
        } catch (IllegalArgumentException e) {
            // Si ocurre un error de validación, se devuelve un error 400.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            // Si ocurre un error de estado, se devuelve un error 503.
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    // Endpoint para buscar usuarios.
    @GetMapping("/buscar")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<List<UsuarioDTO>> buscarUsuarios(@RequestHeader("Authorization") String tokenHeader,
                                                           @RequestParam("q") String q) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        // Se obtiene el id del usuario desde el token.
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        // Si el id es null, se devuelve un error 401.
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Se buscan los usuarios.
        return ResponseEntity.ok(usuarioService.buscarUsuarios(id, q));
    }

    // Endpoint para obtener los seguidos del usuario logueado.
    @GetMapping("/seguidos")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<Set<Long>> getSeguidos(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        // Se obtiene el id del usuario desde el token.
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        // Si el id es null, se devuelve un error 401.
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Se obtienen los seguidos del usuario.
        return ResponseEntity.ok(usuarioService.getSeguidosIds(id));
    }

    // Endpoint para obtener los seguidos del usuario logueado con más detalles.
    @GetMapping("/seguidos/detalle")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<List<UsuarioDTO>> getSeguidosDetalle(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        // Se obtiene el id del usuario desde el token.
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        // Si el id es null, se devuelve un error 401.
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Se obtienen los seguidos del usuario.
        return ResponseEntity.ok(usuarioService.getSeguidos(id));
    }

    // Endpoint para seguir a un usuario.
    @PostMapping("/{id}/seguir")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<String> seguirUsuario(@RequestHeader("Authorization") String tokenHeader,
                                                @PathVariable Long id) {
        try {
            String token = tokenHeader.replace("Bearer ", "").trim();
            // Se obtiene el id del seguidor desde el token.
            Long seguidorId = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
            // Si el seguidorId es null, se devuelve un error 401.
            if (seguidorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            // Se sigue al usuario.
            usuarioService.seguirUsuario(seguidorId, id);
            return ResponseEntity.ok("Ahora sigues a este usuario.");
        } catch (IllegalArgumentException e) {
            // Si ocurre un error de validación, se devuelve un error 400.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para dejar de seguir a un usuario.
    @DeleteMapping("/{id}/seguir")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<String> dejarDeSeguirUsuario(@RequestHeader("Authorization") String tokenHeader,
                                                       @PathVariable Long id) {
        try {
            String token = tokenHeader.replace("Bearer ", "").trim();
            // Se obtiene el id del seguidor desde el token.
            Long seguidorId = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
            // Si el seguidorId es null, se devuelve un error 401.
            if (seguidorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            // Se deja de seguir al usuario.
            usuarioService.dejarDeSeguirUsuario(seguidorId, id);
            return ResponseEntity.ok("Has dejado de seguir a este usuario.");
        } catch (IllegalArgumentException e) {
            // Si ocurre un error de validación, se devuelve un error 400.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para actualizar la biografía del usuario logueado.
    @PutMapping("/me/bio")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<?> updateMiBio(@RequestHeader("Authorization") String tokenHeader,
                                         @Valid @RequestBody UsuarioBioUpdateDTO dto) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            // Se actualiza la biografía del usuario.
            return ResponseEntity.ok(usuarioService.updateBio(id, dto.getBio()));
        } catch (IllegalArgumentException e) {
            // Si ocurre un error de validación, se devuelve un error 400.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Endpoint para actualizar el icono del usuario logueado.
    @PutMapping(value = "/me/icono", consumes = "multipart/form-data")
    // Se obtiene el token del header de la petición.
    public ResponseEntity<?> updateMiIcono(@RequestHeader("Authorization") String tokenHeader,
                                           @RequestParam("iconoFile") MultipartFile iconoFile) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        // Se obtiene el id del usuario desde el token.
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        // Si el id es null, se devuelve un error 401.
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            // Se actualiza el icono del usuario.
            return ResponseEntity.ok(usuarioService.updateIcono(id, iconoFile));
        } catch (IllegalArgumentException e) {
            // Si ocurre un error de validación, se devuelve un error 400.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            // Si ocurre un error de runtime, se devuelve un error 500.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
