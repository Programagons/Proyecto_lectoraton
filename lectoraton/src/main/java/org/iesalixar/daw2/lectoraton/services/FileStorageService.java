package org.iesalixar.daw2.lectoraton.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    // Variable de entorno para la ruta de almacenamiento
    @Value("${UPLOAD_PATH}")
    private String uploadPath;

    /**
     * Guarda un archivo en el sistema de archivos y devuelve el nombre del archivo guardado.
     *
     * @param file El archivo a guardar.
     * @return El nombre del archivo guardado o null si ocurre un error.
     */
    public String saveFile(MultipartFile file) {
        try {
            // Generar un nombre único para el archivo
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID() + "." + fileExtension;

            // Ruta completa del archivo
            Path filePath = Paths.get(uploadPath + File.separator + uniqueFileName);

            // Crear los directorios si no existen
            Files.createDirectories(filePath.getParent());

            // Guardar el archivo en la ruta
            Files.write(filePath, file.getBytes());

            logger.info("Archivo {} guardado con éxito.", uniqueFileName);
            return uniqueFileName; // Devolver el nombre del archivo para guardarlo en la base de datos
        } catch (IOException e) {
            logger.error("Error al guardar el archivo: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Elimina un archivo del sistema de archivos.
     *
     * @param fileName El nombre del archivo a eliminar.
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadPath, fileName);
            Files.deleteIfExists(filePath);
            logger.info("Archivo {} eliminado con éxito.", fileName);
        } catch (IOException e) {
            logger.error("Error al eliminar el archivo {}: {}", fileName, e.getMessage());
        }
    }

    /**
     * Obtiene la extensión del archivo.
     *
     * @param fileName El nombre del archivo.
     * @return La extensión del archivo o una cadena vacía si no tiene extensión.
     */
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return ""; // Sin extensión
        }
    }

    public ResponseEntity<?> readImage(String fileName) {
        try {
            // Construir la ruta completa del archivo a partir del directorio de almacenamiento y el nombre del archivo.
            Path filePath = Paths.get(uploadPath).resolve(fileName).normalize();

            // Crear un recurso a partir de la URI del archivo.
            UrlResource resource = new UrlResource(filePath.toUri());

            // Verificar si el archivo existe y es accesible.
            if (resource.exists() && resource.isReadable()) {
                logger.info("Sirviendo archivo: {}", fileName);

                // Intentar detectar el tipo MIME del archivo.
                String contentType = Files.probeContentType(filePath);

                if (contentType == null) {
                    // Si no se puede determinar el tipo MIME, se asigna un tipo genérico.
                    contentType = "application/octet-stream";
                    logger.warn("No se pudo detectar el tipo MIME del archivo {}. Se usará el tipo genérico.", fileName);
                }
                // Devolver el archivo con el tipo MIME y configuración adecuada para mostrarlo en línea.
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(
                                HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + resource.getFilename() + "\""
                        )
                        .body(resource);
            } else {
                // Si el archivo no existe o no es accesible, devolver un error 404 (NOT FOUND).
                logger.error("El archivo {} no existe o no se puede leer.", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (IOException e) {
            logger.error("Error al servir el archivo {}: {}", fileName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}