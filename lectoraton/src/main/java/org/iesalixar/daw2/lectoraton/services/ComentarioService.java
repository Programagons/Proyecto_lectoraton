package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.ComentarioCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.ComentarioDTO;
import org.iesalixar.daw2.lectoraton.entities.Comentario;
import org.iesalixar.daw2.lectoraton.entities.Resena;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.mappers.ComentarioMapper;
import org.iesalixar.daw2.lectoraton.repositories.ComentarioRepository;
import org.iesalixar.daw2.lectoraton.repositories.ResenaRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioMapper comentarioMapper;
    private final UsuarioService usuarioService;
    private final ActividadUsuarioService actividadUsuarioService;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             ResenaRepository resenaRepository,
                             UsuarioRepository usuarioRepository,
                             ComentarioMapper comentarioMapper,
                             UsuarioService usuarioService,
                             ActividadUsuarioService actividadUsuarioService) {
        this.comentarioRepository = comentarioRepository;
        this.resenaRepository = resenaRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioMapper = comentarioMapper;
        this.usuarioService = usuarioService;
        this.actividadUsuarioService = actividadUsuarioService;
    }

    /**
     * Obtiene los comentarios de una reseña.
     *
     * @param resenaId ID de la reseña.
     * @return Lista de ComentarioDTO con los comentarios de la reseña.
     */
    public List<ComentarioDTO> getByResenaId(Long resenaId) {
        return comentarioRepository.findByResenaIdOrderByFechaCreacionAsc(resenaId).stream()
                .map(comentarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<ComentarioDTO> getById(Long id) {
        return comentarioRepository.findById(id).map(comentarioMapper::toDTO);
    }

    /**
     * Crea un nuevo comentario.
     *
     * @param dto Datos del comentario a crear.
     * @return ComentarioDTO del comentario creado.
     * @throws IllegalArgumentException si la reseña o el usuario no existen.
     */
    public ComentarioDTO create(ComentarioCreateDTO dto) {
        Resena resena = resenaRepository.findById(dto.getResenaId())
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada."));
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Comentario comentario = comentarioMapper.toEntity(dto, resena, usuario);
        comentario.setFechaCreacion(LocalDateTime.now());
        Comentario guardado = comentarioRepository.save(comentario);
        actividadUsuarioService.registrarComentarioNuevo(guardado);
        return comentarioMapper.toDTO(guardado);
    }

    public Long getUsuarioIdAutenticado(String username) {
        return usuarioService.getIdByUsername(username);
    }

    public void delete(Long id) {
        if (!comentarioRepository.existsById(id)) throw new IllegalArgumentException("Comentario no encontrado.");
        comentarioRepository.deleteById(id);
    }
}
