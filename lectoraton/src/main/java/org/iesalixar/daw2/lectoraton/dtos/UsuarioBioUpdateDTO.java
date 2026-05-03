package org.iesalixar.daw2.lectoraton.dtos;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioBioUpdateDTO {

    @Size(max = 255, message = "La biografía no puede superar 255 caracteres.")
    private String bio;
}
