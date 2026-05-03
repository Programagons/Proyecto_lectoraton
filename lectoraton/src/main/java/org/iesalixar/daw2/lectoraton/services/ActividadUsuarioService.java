package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.FeedItemDTO;
import org.iesalixar.daw2.lectoraton.dtos.ProgresoLecturaDTO;
import org.iesalixar.daw2.lectoraton.entities.ActividadUsuario;
import org.iesalixar.daw2.lectoraton.entities.Comentario;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.entities.Resena;
import org.iesalixar.daw2.lectoraton.entities.TipoActividadUsuario;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.repositories.ActividadUsuarioRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class ActividadUsuarioService {

    private static final int TEXTO_MAX = 500;

    private final ActividadUsuarioRepository actividadUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public ActividadUsuarioService(ActividadUsuarioRepository actividadUsuarioRepository,
                                   UsuarioRepository usuarioRepository) {
        this.actividadUsuarioRepository = actividadUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<FeedItemDTO> getFeedParaUsuario(Long usuarioId, boolean incluirPropias, Pageable pageable) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Set<Long> actorIds = new HashSet<>();
        for (Usuario s : usuario.getSeguidos()) {
            actorIds.add(s.getId());
        }
        if (incluirPropias) {
            actorIds.add(usuarioId);
        }
        if (actorIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return actividadUsuarioRepository
                .findByUsuarioActor_IdInOrderByFechaCreacionDesc(actorIds, pageable)
                .map(this::toFeedItem);
    }

    @Transactional
    public void registrarResenaNueva(Resena resena) {
        ActividadUsuario a = new ActividadUsuario();
        a.setUsuarioActor(resena.getUsuario());
        a.setUsuarioDestino(null);
        a.setTipo(TipoActividadUsuario.RESENA);
        a.setLibro(resena.getLibro());
        a.setResena(resena);
        a.setTexto(textoParaResena(resena));
        a.setFechaCreacion(LocalDateTime.now());
        actividadUsuarioRepository.save(a);
    }

    @Transactional
    public void registrarComentarioNuevo(Comentario comentario) {
        Resena resena = comentario.getResena();
        ActividadUsuario a = new ActividadUsuario();
        a.setUsuarioActor(comentario.getUsuario());
        a.setUsuarioDestino(resena.getUsuario());
        a.setTipo(TipoActividadUsuario.COMENTARIO);
        a.setLibro(resena.getLibro());
        a.setResena(resena);
        a.setTexto(truncar(comentario.getContenido(), TEXTO_MAX));
        a.setFechaCreacion(LocalDateTime.now());
        actividadUsuarioRepository.save(a);
    }

    @Transactional
    public void registrarProgresoLectura(Usuario actor, Libro libro, ProgresoLecturaDTO dto) {
        ActividadUsuario a = new ActividadUsuario();
        a.setUsuarioActor(actor);
        a.setUsuarioDestino(null);
        a.setTipo(TipoActividadUsuario.PROGRESO);
        a.setLibro(libro);
        a.setResena(null);
        String estado = dto.getEstado() != null ? dto.getEstado() : "";
        a.setTexto(truncar("Progreso: pág. " + dto.getPaginaActual() + " · " + estado, TEXTO_MAX));
        a.setFechaCreacion(LocalDateTime.now());
        actividadUsuarioRepository.save(a);
    }

    private FeedItemDTO toFeedItem(ActividadUsuario a) {
        FeedItemDTO dto = new FeedItemDTO();
        dto.setId(a.getId());
        dto.setTipo(a.getTipo().name());
        dto.setFechaCreacion(a.getFechaCreacion());
        Usuario actor = a.getUsuarioActor();
        if (actor != null) {
            dto.setActorId(actor.getId());
            dto.setActorUsername(actor.getUsername());
            dto.setActorNombreCompleto(actor.getNombre() + " " + actor.getApellidos());
            dto.setActorIcono(actor.getIcono());
        }
        Libro libro = a.getLibro();
        if (libro != null) {
            dto.setLibroId(libro.getId());
            dto.setLibroTitulo(libro.getTitulo());
            dto.setLibroPortada(libro.getPortada());
            dto.setLibroNumPaginas(libro.getNumPaginas());
        }
        if (a.getResena() != null) {
            dto.setResenaId(a.getResena().getId());
        }
        dto.setTexto(a.getTexto());
        return dto;
    }

    private String textoParaResena(Resena resena) {
        if (resena.getTitulo() != null && !resena.getTitulo().isBlank()) {
            return truncar("Nueva reseña: " + resena.getTitulo().trim(), TEXTO_MAX);
        }
        String tituloLibro = resena.getLibro() != null ? resena.getLibro().getTitulo() : "un libro";
        return truncar("Nueva reseña en \"" + tituloLibro + "\"", TEXTO_MAX);
    }

    private static String truncar(String s, int max) {
        if (s == null) {
            return null;
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }
}
