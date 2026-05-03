package org.iesalixar.daw2.lectoraton.dtos;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AutorCreateDTO {

    /**
     * Nombre completo del autor.
     */
    @NotEmpty(message = "{msg.autor.nombre_completo.notEmpty}")
    @Size(max = 100, message = "{msg.autor.nombre_completo.size}")
    private String nombreCompleto;


    /**
     * Nacionalidad del autor.
     */

    @NotEmpty(message = "{msg.autor.nacionalidad.notEmpty} ")
    @Size(max = 100, message = "{msg.autor.nacionalidad.size}")
    private String nacionalidad;



    private MultipartFile imageFile;


}
