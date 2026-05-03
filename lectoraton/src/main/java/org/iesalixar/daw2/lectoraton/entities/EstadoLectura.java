package org.iesalixar.daw2.lectoraton.entities;

/**
 * Estado de lectura de un libro por un usuario.
 * Corresponde al ENUM de la tabla "usuarios_libros" en el schema.
 */
public enum EstadoLectura {
    quiero_leer,
    leyendo,
    leido,
    abandonado
}
