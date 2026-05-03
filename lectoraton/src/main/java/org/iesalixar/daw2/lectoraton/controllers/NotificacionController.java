package org.iesalixar.daw2.lectoraton.controllers;

import org.iesalixar.daw2.lectoraton.dtos.NotificacionDTO;
import org.iesalixar.daw2.lectoraton.services.NotificacionService;
import org.iesalixar.daw2.lectoraton.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public NotificacionController(NotificacionService notificacionService, UsuarioService usuarioService) {
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/mias")
    public ResponseEntity<List<NotificacionDTO>> getMias(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        return ResponseEntity.ok(notificacionService.getMisNotificaciones(usuarioId));
    }
}
