package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.TropoCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.TropoDTO;
import org.iesalixar.daw2.lectoraton.entities.Tropo;
import org.iesalixar.daw2.lectoraton.mappers.TropoMapper;
import org.iesalixar.daw2.lectoraton.repositories.TropoRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TropoService {

    private final TropoRepository tropoRepository;
    private final TropoMapper tropoMapper;
    private final MessageSource messageSource;

    public TropoService(TropoRepository tropoRepository, TropoMapper tropoMapper, MessageSource messageSource) {
        this.tropoRepository = tropoRepository;
        this.tropoMapper = tropoMapper;
        this.messageSource = messageSource;
    }

    /**
     * Obtiene todos los tropos ordenados por nombre.
     *
     * @return Lista de TropoDTO con los tropos ordenados por nombre.
     */
    public List<TropoDTO> getAllTropos() {
        return tropoRepository.findAllByOrderByNombreAsc().stream()
                .map(tropoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TropoDTO> getTropoById(Long id) {
        return tropoRepository.findById(id).map(tropoMapper::toDTO);
    }

    public TropoDTO createTropo(TropoCreateDTO dto, Locale locale) {
        if (tropoRepository.existsByNombre(dto.getNombre())) {
            String msg = messageSource.getMessage("msg.tropo.nombre.existe", null, locale);
            throw new IllegalArgumentException(msg != null ? msg : "El tropo ya existe.");
        }
        Tropo tropo = tropoMapper.toEntity(dto);
        return tropoMapper.toDTO(tropoRepository.save(tropo));
    }

    public TropoDTO updateTropo(Long id, TropoCreateDTO dto, Locale locale) {
        Tropo existing = tropoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tropo no encontrado."));
        if (tropoRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            String msg = messageSource.getMessage("msg.tropo.nombre.existe", null, locale);
            throw new IllegalArgumentException(msg != null ? msg : "El tropo ya existe.");
        }
        existing.setNombre(dto.getNombre());
        existing.setDescripcion(dto.getDescripcion());
        return tropoMapper.toDTO(tropoRepository.save(existing));
    }

    public void deleteTropo(Long id) {
        if (!tropoRepository.existsById(id)) throw new IllegalArgumentException("Tropo no encontrado.");
        tropoRepository.deleteById(id);
    }
}
