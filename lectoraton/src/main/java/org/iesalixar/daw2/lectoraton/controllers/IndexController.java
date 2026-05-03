package org.iesalixar.daw2.lectoraton.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de bienvenida para la API.
 * Útil para comprobar que el backend está en marcha (p. ej. desde Angular).
 */
@RestController
@RequestMapping("/api")
public class IndexController {

    @GetMapping
    public ResponseEntity<Map<String, String>> apiInfo() {
        return ResponseEntity.ok(Map.of(
                "application", "lectoraton",
                "message", "API disponible. Usa /api/v1/authenticate para login, /api/autores, /api/libros, etc."
        ));
    }
}
