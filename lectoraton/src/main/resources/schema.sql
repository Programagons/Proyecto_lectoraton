-- Tablas dependientes de otras (muchas FK)
DROP TABLE IF EXISTS usuarios_seguidores;
DROP TABLE IF EXISTS bibliotecas_libros;
DROP TABLE IF EXISTS likes_resenas;
DROP TABLE IF EXISTS actividad_usuario;
DROP TABLE IF EXISTS comentarios;
DROP TABLE IF EXISTS resenas;
DROP TABLE IF EXISTS usuarios_libros;
DROP TABLE IF EXISTS libros_tropos;
DROP TABLE IF EXISTS libros_generos;
DROP TABLE IF EXISTS usuarios_roles;


-- Tablas independientes o “padre” de otras
DROP TABLE IF EXISTS bibliotecas;
DROP TABLE IF EXISTS tropos;
DROP TABLE IF EXISTS generos;
DROP TABLE IF EXISTS libros;
DROP TABLE IF EXISTS autores;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS usuarios;


-- USUARIOS

CREATE TABLE usuarios
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username                    VARCHAR(50) UNIQUE  NOT NULL,
    password                    VARCHAR(255)        NOT NULL,
    nombre                      VARCHAR(50)         NOT NULL,
    apellidos                   VARCHAR(100)        NOT NULL,
    email                       VARCHAR(100) UNIQUE NOT NULL,
    bio                         VARCHAR(255),
    icono                       VARCHAR(255),
    code                        VARCHAR(10),
    enabled                     BOOLEAN DEFAULT TRUE NOT NULL,
    fecha_creacion              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    fecha_modificacion_password TIMESTAMP
);

-- ROLES

CREATE TABLE roles
(
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE usuarios_roles
(
    usuario_id BIGINT NOT NULL,
    rol_id     BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- AUTORES

CREATE TABLE autores
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(150) UNIQUE NOT NULL,
    nacionalidad    VARCHAR(100),
    image           VARCHAR(255)
);

-- LIBROS

CREATE TABLE libros
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn              VARCHAR(13) UNIQUE NOT NULL,
    titulo            VARCHAR(200)       NOT NULL,
    saga_nombre       VARCHAR(150),
    numero_saga       INT,
    sinopsis          TEXT,
    autor_id          BIGINT             NOT NULL,
    num_paginas       INT,
    fecha_publicacion DATE,
    portada           VARCHAR(255),

    FOREIGN KEY (autor_id) REFERENCES autores (id)
);

-- GENEROS

CREATE TABLE generos
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE libros_generos
(
    libro_id  BIGINT NOT NULL,
    genero_id BIGINT NOT NULL,

    PRIMARY KEY (libro_id, genero_id),

    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    FOREIGN KEY (genero_id) REFERENCES generos (id) ON DELETE CASCADE
);

-- TROPOS

CREATE TABLE tropos
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE libros_tropos
(
    libro_id BIGINT NOT NULL,
    tropo_id BIGINT NOT NULL,

    PRIMARY KEY (libro_id, tropo_id),

    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE,
    FOREIGN KEY (tropo_id) REFERENCES tropos (id) ON DELETE CASCADE
);

-- ESTADO DE LECTURA

CREATE TABLE usuarios_libros
(
    usuario_id    BIGINT NOT NULL,
    libro_id      BIGINT NOT NULL,

    estado        ENUM('quiero_leer','leyendo','leido','abandonado'),

    pagina_actual INT,
    fecha_inicio  DATE,
    fecha_fin     DATE,

    calificacion  INT CHECK (calificacion BETWEEN 1 AND 5),

    fecha_actualizacion TIMESTAMP NULL,

    PRIMARY KEY (usuario_id, libro_id),

    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE
);

-- RESEÑAS

CREATE TABLE resenas
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    usuario_id     BIGINT NOT NULL,
    libro_id       BIGINT NOT NULL,

    titulo         VARCHAR(200),
    contenido      TEXT,
    calificacion   INT CHECK (calificacion BETWEEN 1 AND 5),
    contiene_spoiler BOOLEAN DEFAULT FALSE,

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE
);

-- COMENTARIOS EN RESEÑAS

CREATE TABLE comentarios
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    resena_id      BIGINT NOT NULL,
    usuario_id     BIGINT NOT NULL,

    contenido      TEXT   NOT NULL,
    contiene_spoiler BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (resena_id) REFERENCES resenas (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

-- FEED / ACTIVIDAD (reseñas, progreso, comentarios)

CREATE TABLE actividad_usuario
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_actor_id   BIGINT NOT NULL,
    usuario_destino_id BIGINT,
    tipo                 ENUM ('RESENA', 'PROGRESO', 'COMENTARIO') NOT NULL,
    libro_id             BIGINT,
    resena_id            BIGINT,
    texto                VARCHAR(500),
    fecha_creacion       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_actor_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_destino_id) REFERENCES usuarios (id) ON DELETE SET NULL,
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE SET NULL,
    FOREIGN KEY (resena_id) REFERENCES resenas (id) ON DELETE SET NULL
);

-- LIKES EN RESEÑAS

CREATE TABLE likes_resenas
(
    usuario_id BIGINT NOT NULL,
    resena_id  BIGINT NOT NULL,

    PRIMARY KEY (usuario_id, resena_id),

    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    FOREIGN KEY (resena_id) REFERENCES resenas (id) ON DELETE CASCADE
);

-- SEGUIR USUARIOS

CREATE TABLE usuarios_seguidores
(
    seguidor_id BIGINT NOT NULL,
    seguido_id  BIGINT NOT NULL,

    PRIMARY KEY (seguidor_id, seguido_id),

    FOREIGN KEY (seguidor_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    FOREIGN KEY (seguido_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

-- BIBLIOTECAS

CREATE TABLE bibliotecas
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT       NOT NULL,
    nombre     VARCHAR(100) NOT NULL,

    UNIQUE KEY uk_bibliotecas_usuario_nombre (usuario_id, nombre),
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE bibliotecas_libros
(
    biblioteca_id  BIGINT NOT NULL,
    libro_id       BIGINT NOT NULL,
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (biblioteca_id, libro_id),

    FOREIGN KEY (biblioteca_id) REFERENCES bibliotecas (id) ON DELETE CASCADE,
    FOREIGN KEY (libro_id) REFERENCES libros (id) ON DELETE CASCADE
);

-- INDICES (RENDIMIENTO)

CREATE INDEX idx_libros_autor
    ON libros (autor_id);

CREATE INDEX idx_resenas_libro
    ON resenas (libro_id);

CREATE INDEX idx_resenas_usuario
    ON resenas (usuario_id);

CREATE INDEX idx_usuarios_libros_usuario
    ON usuarios_libros (usuario_id);

CREATE INDEX idx_usuarios_libros_libro
    ON usuarios_libros (libro_id);

CREATE INDEX idx_actividad_usuario_fecha
    ON actividad_usuario (fecha_creacion);

CREATE INDEX idx_actividad_usuario_actor
    ON actividad_usuario (usuario_actor_id);

-- Para no olvidarme
-- Las calificaciones las podríamos calcular rollo:
-- SELECT AVG(calificacion)
-- FROM usuarios_libros
-- WHERE libro_id = 10;