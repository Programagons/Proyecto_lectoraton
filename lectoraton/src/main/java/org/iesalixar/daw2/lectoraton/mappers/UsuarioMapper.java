package org.iesalixar.daw2.lectoraton.mappers;

import org.iesalixar.daw2.lectoraton.dtos.UsuarioCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioDTO;
import org.iesalixar.daw2.lectoraton.dtos.UsuarioPerfilDTO;
import org.iesalixar.daw2.lectoraton.entities.Role;
import org.iesalixar.daw2.lectoraton.entities.Usuario;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombre(usuario.getNombre());
        dto.setApellidos(usuario.getApellidos());
        dto.setEmail(usuario.getEmail());
        dto.setBio(usuario.getBio());
        dto.setIcono(usuario.getIcono());
        return dto;
    }

    public UsuarioPerfilDTO toPerfilDTO(Usuario usuario) {
        UsuarioPerfilDTO dto = new UsuarioPerfilDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombre(usuario.getNombre());
        dto.setApellidos(usuario.getApellidos());
        dto.setEmail(usuario.getEmail());
        dto.setBio(usuario.getBio());
        dto.setIcono(usuario.getIcono());
        List<String> etiquetas = usuario.getRoles().stream()
                .map(Role::getNombre)
                .map(this::rolEtiquetaLegible)
                .sorted(Comparator.naturalOrder())
                .toList();
        dto.setRolesEtiqueta(etiquetas);
        return dto;
    }

    private String rolEtiquetaLegible(String nombreRol) {
        if (nombreRol == null) {
            return "";
        }
        return switch (nombreRol) {
            case "Admin" -> "Administrador";
            case "Editor" -> "Editor";
            case "Lector" -> "Lector";
            default -> nombreRol;
        };
    }

    public Usuario toEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setUsername(dto.getUsername());
        usuario.setNombre(dto.getNombre());
        usuario.setApellidos(dto.getApellidos());
        usuario.setEmail(dto.getEmail());
        usuario.setBio(dto.getBio());
        usuario.setIcono(dto.getIcono());
        return usuario;
    }

    public Usuario toEntity(UsuarioCreateDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setNombre(dto.getNombre());
        usuario.setApellidos(dto.getApellidos());
        usuario.setEmail(dto.getEmail());
        usuario.setBio(dto.getBio());
        usuario.setIcono(dto.getIcono());
        usuario.setEnabled(true);
        return usuario;
    }
}
