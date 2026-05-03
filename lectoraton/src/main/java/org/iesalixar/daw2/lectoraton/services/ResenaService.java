package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.ResenaCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.ResenaDTO;
import org.iesalixar.daw2.lectoraton.dtos.ResenaUpdateDTO;
import org.iesalixar.daw2.lectoraton.entities.EstadoLectura;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.entities.Resena;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibro;
import org.iesalixar.daw2.lectoraton.entities.UsuarioLibroId;
import org.iesalixar.daw2.lectoraton.mappers.ResenaMapper;
import org.iesalixar.daw2.lectoraton.repositories.LibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.ResenaRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioLibroRepository;
import org.iesalixar.daw2.lectoraton.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final UsuarioLibroRepository usuarioLibroRepository;
    private final ResenaMapper resenaMapper;
    private final ActividadUsuarioService actividadUsuarioService;

    public ResenaService(ResenaRepository resenaRepository,
                         UsuarioRepository usuarioRepository,
                         LibroRepository libroRepository,
                         UsuarioLibroRepository usuarioLibroRepository,
                         ResenaMapper resenaMapper,
                         ActividadUsuarioService actividadUsuarioService) {
        this.resenaRepository = resenaRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.usuarioLibroRepository = usuarioLibroRepository;
        this.resenaMapper = resenaMapper;
        this.actividadUsuarioService = actividadUsuarioService;
    }

    public Page<ResenaDTO> getByLibroId(Long libroId, Pageable pageable) {
        return resenaRepository.findByLibroId(libroId, pageable).map(resenaMapper::toDTO);
    }

    public Page<ResenaDTO> getByUsuarioId(Long usuarioId, Pageable pageable) {
        return resenaRepository.findByUsuarioId(usuarioId, pageable).map(resenaMapper::toDTO);
    }

    public Page<ResenaDTO> buscarEnLibro(Long libroId, String texto, Pageable pageable, Long usuarioActualId) {
        return resenaRepository.buscarEnLibro(libroId, texto, pageable)
                .map(resena -> toDTOConContextoUsuario(resena, usuarioActualId));
    }

    public Optional<ResenaDTO> getById(Long id) {
        return resenaRepository.findById(id).map(resenaMapper::toDTO);
    }

    public Optional<ResenaDTO> getMiResenaEnLibro(Long usuarioId, Long libroId) {
        return resenaRepository.findByUsuarioIdAndLibroId(usuarioId, libroId).map(resenaMapper::toDTO);
    }

    public ResenaDTO create(ResenaCreateDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Libro libro = libroRepository.findById(dto.getLibroId())
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));
        if (resenaRepository.existsByUsuarioIdAndLibroId(dto.getUsuarioId(), dto.getLibroId())) {
            throw new IllegalArgumentException("Ya existe una reseña de este usuario para este libro.");
        }
        Resena resena = resenaMapper.toEntity(dto, usuario, libro);
        resena.setFechaCreacion(LocalDateTime.now());
        Resena guardada = resenaRepository.save(resena);
        actividadUsuarioService.registrarResenaNueva(guardada);

        if (dto.getCalificacion() != null) {
            UsuarioLibro usuarioLibro = usuarioLibroRepository.findByUsuarioIdAndLibroId(dto.getUsuarioId(), dto.getLibroId())
                    .orElseGet(() -> {
                        UsuarioLibro nuevo = new UsuarioLibro();
                        nuevo.setId(new UsuarioLibroId(dto.getUsuarioId(), dto.getLibroId()));
                        nuevo.setUsuario(usuario);
                        nuevo.setLibro(libro);
                        return nuevo;
                    });
            if (usuarioLibro.getEstado() == null) {
                usuarioLibro.setEstado(EstadoLectura.leido);
            }
            usuarioLibro.setCalificacion(dto.getCalificacion());
            usuarioLibroRepository.save(usuarioLibro);
        }

        return resenaMapper.toDTO(guardada);
    }

    public void calificarLibro(Long usuarioId, Long libroId, Integer calificacion) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));

        UsuarioLibro usuarioLibro = usuarioLibroRepository.findByUsuarioIdAndLibroId(usuarioId, libroId)
                .orElseGet(() -> {
                    UsuarioLibro nuevo = new UsuarioLibro();
                    nuevo.setId(new UsuarioLibroId(usuarioId, libroId));
                    nuevo.setUsuario(usuario);
                    nuevo.setLibro(libro);
                    return nuevo;
                });

        if (usuarioLibro.getEstado() == null) {
            usuarioLibro.setEstado(EstadoLectura.leido);
        }
        usuarioLibro.setCalificacion(calificacion);
        usuarioLibroRepository.save(usuarioLibro);
    }

    public void delete(Long id) {
        if (!resenaRepository.existsById(id)) throw new IllegalArgumentException("Reseña no encontrada.");
        resenaRepository.deleteById(id);
    }

    public void deletePropia(Long id, Long usuarioId) {
        Resena resena = resenaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada."));
        if (!resena.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Solo puedes eliminar tus propias reseñas.");
        }
        resenaRepository.delete(resena);
    }

    public ResenaDTO updatePropia(Long id, Long usuarioId, ResenaUpdateDTO dto) {
        Resena resena = resenaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada."));
        if (!resena.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Solo puedes editar tus propias reseñas.");
        }

        resena.setTitulo(dto.getTitulo() == null ? null : dto.getTitulo().trim());
        resena.setContenido(dto.getContenido() == null ? null : dto.getContenido().trim());
        resena.setContieneSpoiler(Boolean.TRUE.equals(dto.getContieneSpoiler()));
        resena.setCalificacion(dto.getCalificacion());
        Resena guardada = resenaRepository.save(resena);

        UsuarioLibro usuarioLibro = usuarioLibroRepository.findByUsuarioIdAndLibroId(usuarioId, resena.getLibro().getId())
                .orElseGet(() -> {
                    UsuarioLibro nuevo = new UsuarioLibro();
                    nuevo.setId(new UsuarioLibroId(usuarioId, resena.getLibro().getId()));
                    nuevo.setUsuario(resena.getUsuario());
                    nuevo.setLibro(resena.getLibro());
                    return nuevo;
                });
        if (usuarioLibro.getEstado() == null) {
            usuarioLibro.setEstado(EstadoLectura.leido);
        }
        usuarioLibro.setCalificacion(dto.getCalificacion());
        usuarioLibroRepository.save(usuarioLibro);

        return resenaMapper.toDTO(guardada);
    }

    public ResenaDTO toggleLike(Long resenaId, Long usuarioId) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada."));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (resena.getUsuariosQueDieronLike().stream().anyMatch(u -> u.getId().equals(usuarioId))) {
            resena.getUsuariosQueDieronLike().removeIf(u -> u.getId().equals(usuarioId));
        } else {
            resena.getUsuariosQueDieronLike().add(usuario);
        }

        return toDTOConContextoUsuario(resenaRepository.save(resena), usuarioId);
    }

    private ResenaDTO toDTOConContextoUsuario(Resena resena, Long usuarioActualId) {
        ResenaDTO dto = resenaMapper.toDTO(resena);
        dto.setLikedByCurrentUser(
                usuarioActualId != null
                        && resena.getUsuariosQueDieronLike() != null
                        && resena.getUsuariosQueDieronLike().stream().anyMatch(u -> u.getId().equals(usuarioActualId))
        );
        return dto;
    }
}
