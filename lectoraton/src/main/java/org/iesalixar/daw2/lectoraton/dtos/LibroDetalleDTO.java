package org.iesalixar.daw2.lectoraton.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class LibroDetalleDTO {
    private Long id;
    private String titulo;
    private String sagaNombre;
    private Integer numeroSaga;
    private String autorNombre;
    private String sinopsis;
    private String portada;
    private List<String> generos;
    private List<String> tropos;
    private Integer paginas;
    private LocalDate fechaPublicacion;
    private boolean yaEnAlgunaBiblioteca;
    private ProgresoLecturaDTO miProgreso;
    private ResenaResumenDTO resumenResenas;
    private List<UsuarioMiniDTO> amigosQueHanLeido;
    private List<LibroMiniDTO> otrosMismoAutor;
    private List<LibroMiniDTO> otrosParecidos;
}
