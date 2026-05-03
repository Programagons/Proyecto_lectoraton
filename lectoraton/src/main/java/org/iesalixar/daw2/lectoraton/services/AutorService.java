package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.AutorCreateDTO;
import org.iesalixar.daw2.lectoraton.dtos.AutorDTO;
import org.iesalixar.daw2.lectoraton.entities.Autor;
import org.iesalixar.daw2.lectoraton.mappers.AutorMapper;
import org.iesalixar.daw2.lectoraton.repositories.AutorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class AutorService {

    private static final Logger logger = LoggerFactory.getLogger(AutorService.class);

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private AutorMapper autorMapper;

    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Obtiene todos los autores con paginación y los convierte en una página de AutorDTO.
     *
     * @param pageable Objeto de paginación que define la página, el tamaño y la ordenación.
     * @return Página de AutorDTO
     *
     */

    public Page<AutorDTO> getAllAutores(Pageable pageable) {
        logger.info("Solicitando todas las autores con paginación: página {}, tamaño{}",
                pageable.getPageNumber(), pageable.getPageSize());
        try{
            Page<Autor> autores = autorRepository.findAll(pageable);
            logger.info("Se han encontrado {} autores en la página actual.", autores.getNumberOfElements());
            return autores.map(autorMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al obtener la lista paginada de autores: {}", e.getMessage());
            throw e;
        }
    }


    /**
     * Busca una autor específica por su ID.
     *
     * @param id Identificador único de la autor.
     * @return Un Optional que contiene un 'AutorDTO' si la autor existe.
     */

    public Optional<AutorDTO> getAutorById(Long id) {
        try {
            logger.info("Buscando autor con ID {}...");
            return autorRepository.findById(id).map(autorMapper::toDTO);
        } catch (Exception e) {
            logger.error("Error al buscar autor con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar el autor.", e);

        }
    }

    /**
     *
     * Crea un nuevo autor en la base de datos.
     *
     * @param autorCreateDTO DTO que contiene los datos del autor a crear.
     * @param locale          Idioma para los mensajes de error.
     * @return Respuesta HTTP con el estado de la operación.
     * @throws IllegalArgumentException Si el código ya existe.
     *
     */

    public AutorDTO createAutor(AutorCreateDTO autorCreateDTO, Locale locale) {
        if (autorRepository.existsByNombreCompleto(autorCreateDTO.getNombreCompleto())) {
            String errorMessage = messageSource.getMessage("msg.autor-controller.insert.codeExist", null, locale);
            throw new IllegalArgumentException(errorMessage);
        }


        // Procesar la imagen si se proporciona
        String fileName = null;
        if (autorCreateDTO.getImageFile() != null && !autorCreateDTO.getImageFile().isEmpty()){
            fileName = fileStorageService.saveFile(autorCreateDTO.getImageFile());
            if (fileName == null) {
                throw new RuntimeException("Error al guardar la imagen.");
            }
        }

        // Se convierte a Entity para almacenar en la base de datos
        Autor autor = autorMapper.toEntity(autorCreateDTO);
        autor.setImage(fileName);

        Autor savedAutor = autorRepository.save(autor);
        logger.info("Autor creada exitosamente con ID {}", savedAutor.getId());
        // Se devuelve el DTO
        return autorMapper.toDTO(savedAutor);
    }


    /**
     * @param id              Identificador del autor a actualizar.
     * @param autorCreateDTO DTO que contiene los nuevos datos del autor.
     * @param locale          idioma para los mensajes de error.
     * @return DTO del autor actualizado.
     * @throws IllegalArgumentException Si el autor no existe.
     *
     */

    public AutorDTO updateAutor(Long id, AutorCreateDTO autorCreateDTO, Locale locale) {
        logger.info("Actualizando autor con ID {}", id);
        // Buscar la autor existente
        Autor existingAutor = autorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El autor no existe."));

        if (autorRepository.existsByNombreCompletoAndIdNot(autorCreateDTO.getNombreCompleto(), id)){
            String errorMessage = messageSource.getMessage("msg.autor-controller.update.codeExist", null, locale);
            throw new IllegalArgumentException(errorMessage);
        }

        // Procesar la imagen si se proporciona
        String fileName = existingAutor.getImage(); // Conservar la imagen existente por defecto.
        if (autorCreateDTO.getImageFile() != null && !autorCreateDTO.getImageFile().isEmpty()){
            fileName = fileStorageService.saveFile(autorCreateDTO.getImageFile());
            if (fileName == null){
                throw new RuntimeException("Error al guardar la imagen.");
            }
        }

        // Actualizar los datos del autor
        existingAutor.setNombreCompleto(autorCreateDTO.getNombreCompleto());
        existingAutor.setNacionalidad(autorCreateDTO.getNacionalidad());
        existingAutor.setImage(fileName);
        // Guardar los cambios
        Autor updatedAutor = autorRepository.save(existingAutor);
        logger.info("Autor con ID {} actualizada exitosamente", updatedAutor.getId());

        return autorMapper.toDTO(updatedAutor);
    }

    /**
     * Elimina un autor específico por su ID.
     *
     * @param id identificador único del autor.
     * @return IllegalArgumentException Si la autor no existe.
     */

    public void deleteAutor(Long id) {
        logger.info("Buscando autor con ID {}", id);

        // Buscar la autor
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El autor no existe."));

        // Eliminar la imagen asociada si existe
        if (autor.getImage() != null && !autor.getImage().isEmpty()){
            fileStorageService.deleteFile(autor.getImage());
            logger.info("Imagen asociada a la autor con ID {} eliminada.", id);
        }

        // Eliminar el autor
        autorRepository.deleteById(id);
        logger.info("Autor con ID {} eliminado exitosamente.", id);
    }

}
