package org.iesalixar.daw2.lectoraton.controllers;

import org.iesalixar.daw2.lectoraton.dtos.FeedItemDTO;
import org.iesalixar.daw2.lectoraton.services.ActividadUsuarioService;
import org.iesalixar.daw2.lectoraton.services.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final ActividadUsuarioService actividadUsuarioService;
    private final UsuarioService usuarioService;

    public FeedController(ActividadUsuarioService actividadUsuarioService,
                          UsuarioService usuarioService) {
        this.actividadUsuarioService = actividadUsuarioService;
        this.usuarioService = usuarioService;
    }

    /**
     * Actividad reciente de usuarios que sigues (y opcionalmente la tuya).
     */
    @GetMapping("/mio")
    public ResponseEntity<?> getFeedMio(
            @RequestParam(value = "incluirPropias", defaultValue = "true") boolean incluirPropias,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long usuarioId = usuarioService.getIdByUsername(userDetails.getUsername());
        Page<FeedItemDTO> page = actividadUsuarioService.getFeedParaUsuario(usuarioId, incluirPropias, pageable);
        return ResponseEntity.ok(page);
    }
}
