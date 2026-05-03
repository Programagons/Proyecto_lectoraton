package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.GeneroCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.GeneroDTO;
import org.iesalixar.daw2.lectoraton.entities.Genero;
import org.iesalixar.daw2.lectoraton.mappers.GeneroMapper;
import org.iesalixar.daw2.lectoraton.repositories.GeneroRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GeneroService {

    private final GeneroRepository generoRepository;
    private final GeneroMapper generoMapper;
    private final MessageSource messageSource;

    public GeneroService(GeneroRepository generoRepository, GeneroMapper generoMapper, MessageSource messageSource) {
        this.generoRepository = generoRepository;
        this.generoMapper = generoMapper;
        this.messageSource = messageSource;
    }

    public List<GeneroDTO> getAllGeneros() {
        return generoRepository.findAllByOrderByNombreAsc().stream()
                .map(generoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<GeneroDTO> getGeneroById(Long id) {
        return generoRepository.findById(id).map(generoMapper::toDTO);
    }

    public GeneroDTO createGenero(GeneroCreateDTO dto, Locale locale) {
        if (generoRepository.existsByNombre(dto.getNombre())) {
            String msg = messageSource.getMessage("msg.genero.nombre.existe", null, locale);
            throw new IllegalArgumentException(msg != null ? msg : "El género ya existe.");
        }
        Genero genero = generoMapper.toEntity(dto);
        return generoMapper.toDTO(generoRepository.save(genero));
    }

    public GeneroDTO updateGenero(Long id, GeneroCreateDTO dto, Locale locale) {
        Genero existing = generoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Género no encontrado."));
        if (generoRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            String msg = messageSource.getMessage("msg.genero.nombre.existe", null, locale);
            throw new IllegalArgumentException(msg != null ? msg : "El género ya existe.");
        }
        existing.setNombre(dto.getNombre());
        existing.setDescripcion(dto.getDescripcion());
        return generoMapper.toDTO(generoRepository.save(existing));
    }

    public void deleteGenero(Long id) {
        if (!generoRepository.existsById(id)) throw new IllegalArgumentException("Género no encontrado.");
        generoRepository.deleteById(id);
    }
}
