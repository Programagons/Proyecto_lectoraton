package org.iesalixar.daw2.lectoraton.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.lectoraton.dtos.AuthRequestDTO;
import org.iesalixar.daw2.lectoraton.dtos.AuthResponseDTO;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioCreateDTO;
import org.iesalixar.daw2.lectoraton.services.UsuarioService;
import org.iesalixar.daw2.lectoraton.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;
    private final Environment environment;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtUtil jwtUtil,
                                    UsuarioService usuarioService,
                                    Environment environment) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
        this.environment = environment;
    }

// Endpoint auxiliar para que frontend sepa si mostrar botón Google OAuth.
/* Se obtiene el cliente ID y el secret del archivo application.properties */
    @GetMapping("/oauth/google-enabled")
    public ResponseEntity<Map<String, Boolean>> oauthGoogleConfigured() {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String secret = environment.getProperty("spring.security.oauth2.client.registration.google.client-secret");
        boolean enabled = StringUtils.hasText(clientId) && StringUtils.hasText(secret);
        return ResponseEntity.ok(Map.of("googleEnabled", enabled));
    }

// Endpoint de login: valida credenciales y devuelve JWT firmado.
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO authRequest) {
        /* Se valida que el nombre de usuario y la contraseña no sean nulos */
        try {
            if (authRequest.getUsername() == null || authRequest.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponseDTO(null, "El nombre de usuario y la contraseña son obligatorios."));
            }
            /* Se autentica el usuario */
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            String username = authentication.getName();
            /* Se obtienen los roles del usuario */
            List<String> roles = authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList();
            /* Se genera el token JWT */
            String token = jwtUtil.generateToken(username, roles, usuarioService.getIdByUsername(username));
            return ResponseEntity.ok(new AuthResponseDTO(token, "Authentication successful"));
        } catch (BadCredentialsException e) {
            /* Se devuelve un error 401 si las credenciales son inválidas */
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, "Credenciales inválidas. Por favor, verifica tus datos."));
        } catch (Exception e) {
            /* Se devuelve un error 500 si ocurre un error inesperado */
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponseDTO(null, "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde."));
        }
    }
// Endpoint de registro: crea usuario con rol por defecto.
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UsuarioCreateDTO dto) {
        try {
            /* Se crea el usuario */
            usuarioService.create(dto);
            /* Se devuelve un error 400 si ocurre un error de validación */
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponseDTO(null, "Usuario registrado correctamente. Inicia sesión en /authenticate."));
        } catch (IllegalArgumentException e) {
            /* Se devuelve un error 400 si ocurre un error de validación */
            return ResponseEntity.badRequest().body(new AuthResponseDTO(null, e.getMessage()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponseDTO> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponseDTO(null, "Ocurrió un error inesperado: " + e.getMessage()));
    }
}
