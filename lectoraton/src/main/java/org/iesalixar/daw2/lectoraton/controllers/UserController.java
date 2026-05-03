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

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Controlador del usuario autenticado (usa la entidad Usuario / tabla usuarios).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UserController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilDTO> getUsuarioLogueado(@RequestHeader("Authorization") String tokenHeader) {
        logger.info("Solicitando la información del usuario logueado");
        String token = tokenHeader.replace("Bearer ", "").trim();
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<UsuarioPerfilDTO> opt = usuarioService.getUsuarioPerfil(id);
        return opt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/me/solicitud-editor")
    public ResponseEntity<String> solicitarRolEditor(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            usuarioService.solicitarRolEditor(id);
            return ResponseEntity.ok("Solicitud enviada. Revisa tu correo de administrador para aprobarla.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscarUsuarios(@RequestHeader("Authorization") String tokenHeader,
                                                           @RequestParam("q") String q) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(usuarioService.buscarUsuarios(id, q));
    }

    @GetMapping("/seguidos")
    public ResponseEntity<Set<Long>> getSeguidos(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(usuarioService.getSeguidosIds(id));
    }

    @GetMapping("/seguidos/detalle")
    public ResponseEntity<List<UsuarioDTO>> getSeguidosDetalle(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(usuarioService.getSeguidos(id));
    }

    @PostMapping("/{id}/seguir")
    public ResponseEntity<String> seguirUsuario(@RequestHeader("Authorization") String tokenHeader,
                                                @PathVariable Long id) {
        try {
            String token = tokenHeader.replace("Bearer ", "").trim();
            Long seguidorId = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
            if (seguidorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            usuarioService.seguirUsuario(seguidorId, id);
            return ResponseEntity.ok("Ahora sigues a este usuario.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/seguir")
    public ResponseEntity<String> dejarDeSeguirUsuario(@RequestHeader("Authorization") String tokenHeader,
                                                       @PathVariable Long id) {
        try {
            String token = tokenHeader.replace("Bearer ", "").trim();
            Long seguidorId = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
            if (seguidorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            usuarioService.dejarDeSeguirUsuario(seguidorId, id);
            return ResponseEntity.ok("Has dejado de seguir a este usuario.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/me/bio")
    public ResponseEntity<?> updateMiBio(@RequestHeader("Authorization") String tokenHeader,
                                         @Valid @RequestBody UsuarioBioUpdateDTO dto) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(usuarioService.updateBio(id, dto.getBio()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
