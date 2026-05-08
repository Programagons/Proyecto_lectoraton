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

/**
 * Servicio de negocio para operaciones CRUD de géneros.
 */
public class GeneroService {

    private final GeneroRepository generoRepository;
    // Repositorio de géneros.
    private final GeneroMapper generoMapper;
    // Mapeador de géneros.
    private final MessageSource messageSource;
    // Fuente de mensajes.

    /**
     * Constructor del servicio de géneros.
     * @param generoRepository Repositorio de géneros.
     * @param generoMapper Mapeador de géneros.
     * @param messageSource Fuente de mensajes.
     */
    public GeneroService(GeneroRepository generoRepository, GeneroMapper generoMapper, MessageSource messageSource) {
        this.generoRepository = generoRepository;
        this.generoMapper = generoMapper;
        this.messageSource = messageSource;
    }


    /**
     * Obtiene todos los géneros ordenados por nombre.
     * @return Lista de géneros.
     */
    public List<GeneroDTO> getAllGeneros() {
        // Se obtienen todos los géneros ordenados por nombre.
        return generoRepository.findAllByOrderByNombreAsc().stream()
                .map(generoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<GeneroDTO> getGeneroById(Long id) {
        // Se obtiene el género por id.
        return generoRepository.findById(id).map(generoMapper::toDTO);
    }

    public GeneroDTO createGenero(GeneroCreateDTO dto, Locale locale) {
        // Se verifica si el género ya existe.
        if (generoRepository.existsByNombre(dto.getNombre())) {
            String msg = messageSource.getMessage("msg.genero.nombre.existe", null, locale);
            throw new IllegalArgumentException(msg != null ? msg : "El género ya existe.");
        }
        // Se crea el género.
        Genero genero = generoMapper.toEntity(dto);
        return generoMapper.toDTO(generoRepository.save(genero));
    }

    public GeneroDTO updateGenero(Long id, GeneroCreateDTO dto, Locale locale) {
        // Se obtiene el género existente.
        Genero existing = generoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Género no encontrado."));
        // Se verifica si el género ya existe.
        if (generoRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            String msg = messageSource.getMessage("msg.genero.nombre.existe", null, locale);
            throw new IllegalArgumentException(msg != null ? msg : "El género ya existe.");
        }
        // Se actualiza el género.
        existing.setNombre(dto.getNombre());
        existing.setDescripcion(dto.getDescripcion());
        return generoMapper.toDTO(generoRepository.save(existing));
    }

    public void deleteGenero(Long id) {
        // Se verifica si el género existe.
        if (!generoRepository.existsById(id)) throw new IllegalArgumentException("Género no encontrado.");
        generoRepository.deleteById(id);
    }
}
