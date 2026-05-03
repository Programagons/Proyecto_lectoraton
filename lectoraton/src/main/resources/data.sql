SET NAMES utf8mb4;

-- Contraseña de prueba (todas iguales): coincide con BCryptPasswordEncoder de Spring
-- Hash BCrypt para la misma contraseña usada en desarrollo del proyecto
INSERT INTO usuarios (username, password, nombre, apellidos, email, bio, icono)
VALUES ('gons', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Gonzalo', 'Pulido',
        'programagons@gmail.com', 'Lector empedernido; mezclo fantasía y CF.', NULL),
       ('ales', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Alejandro', 'Álvarez',
        'alejandro@gmail.com', 'Amante de la ciencia ficción y space opera.', NULL),
       ('ash', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Ash', 'Allen',
        'ashallen@gmail.com', 'Fantasía urbana y true crime literario.', NULL),
       ('maria_reads', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'María', 'López',
        'maria.reads@example.com', 'BookTok de viernes noche.', NULL),
       ('carlos_m', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Carlos', 'Méndez',
        'carlos.m@example.com', 'Juvenil y sagas largas.', NULL),
       ('lucia_f', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Lucía', 'Fernández',
        'lucia.f@example.com', 'Mitología, romance y tragedias griegas.', NULL),
       ('hugo_r', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Hugo', 'Ramírez',
        'hugo.r@example.com', 'Campamento Mestizo mood.', NULL),
       ('elena_t', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Elena', 'Torres',
        'elena.t@example.com', 'Distopías y survival.', NULL),
       ('andrea_s', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Andrea', 'Silva',
        'andrea.s@example.com', 'Shadowhunters desde la primera página.', NULL),
       ('pablo_v', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Pablo', 'Vega',
        'pablo.v@example.com', 'Explorador de géneros; reseñas breves.', NULL),
       ('marta_i', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Marta', 'Iglesias',
        'marta.i@example.com', 'Releyendo Miller cada invierno.', NULL),
       ('irene_c', '$2a$12$PKj4ngV7RNqpbXC8uuxWU.Nq1BL2kK1DJn5sLM1m7V9fJ.je9CWqC', 'Irene', 'Castro',
        'irene.c@example.com', 'Found family y casas mágicas.', NULL);

INSERT INTO roles (nombre)
VALUES ('Admin'),
       ('Lector'),
       ('Editor');

INSERT INTO usuarios_roles (usuario_id, rol_id)
VALUES (1, 1),
       (2, 2), (3, 2), (4, 2), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2), (10, 2), (11, 2), (12, 2);

INSERT INTO autores (nombre_completo, nacionalidad, image)
VALUES ('C.S. Pacat', 'Australiana', 'https://images.gr-assets.com/authors/1630402142p5/6616333.jpg'),
       ('Andy Weir', 'Estadounidense', 'https://images.gr-assets.com/authors/1579185418p5/6540057.jpg'),
       ('R.F. Kuang', 'Estadounidense', 'https://images.gr-assets.com/authors/1527010084p5/14800486.jpg'),
       ('Suzanne Collins', 'Estadounidense', NULL),
       ('Rick Riordan', 'Estadounidense', NULL),
       ('T.J. Klune', 'Estadounidense', NULL),
       ('Madeline Miller', 'Estadounidense', NULL),
       ('Cassandra Clare', 'Estadounidense', NULL);

INSERT INTO libros (isbn, titulo, saga_nombre, numero_saga, sinopsis, autor_id, num_paginas, fecha_publicacion, portada)
VALUES ('9788416517756', 'El Rey Oscuro', 'Una herencia oscura', 1,
        'Will descubre magia y oscuridad en el Londres del siglo XIX.', 1, 448, '2022-05-01',
        'https://m.media-amazon.com/images/I/812wNzwscvL._UF1000,1000_QL80_.jpg'),
       ('9788419030825', 'El heredero oscuro', 'Una herencia oscura', 2,
        'La oscuridad crece y Will debe enfrentarse a secretos familiares.', 1, 480, '2024-03-12',
        'https://imagessl4.casadellibro.com/a/l/s7/25/9788419030825.webp'),
       ('9780593135204', 'Project Hail Mary', NULL, NULL,
        'Ryland Grace debe salvar a la humanidad de una amenaza estelar.', 2, 496, '2021-05-04',
        'https://images-na.ssl-images-amazon.com/images/P/0593135202.01.L.jpg'),
       ('9780063021426', 'Babel', NULL, NULL,
        'Oxford, traducción y poder imperial en una fantasía histórica.', 3, 545, '2022-08-23',
        'https://images-na.ssl-images-amazon.com/images/P/0063021420.01.L.jpg'),
       ('9788427202124', 'Los juegos del hambre', 'Los Juegos del Hambre', 1,
        'Katniss se ofrece tributo en lugar de su hermana en los crueltísimos juegos del Capitolio.', 4, 374,
        '2008-09-01', 'https://m.media-amazon.com/images/I/71BUB4ubNEL._UF1000,1000_QL80_.jpg'),
       ('9788427202131', 'En llamas', 'Los Juegos del Hambre', 2,
        'El régimen prepara un vasallaje especial que arrastra de nuevo a Katniss al arena.', 4, 472,
        '2009-09-08', 'https://m.media-amazon.com/images/I/71Bdjbds7qL.jpg'),
       ('9788427202148', 'Sinsajo', 'Los Juegos del Hambre', 3,
        'La rebelión contra el Capitolio estalla y Katniss se convierte en símbolo.', 4, 390,
        '2010-08-24', 'https://m.media-amazon.com/images/I/71DvoGObFpL._UF1000,1000_QL80_.jpg'),
       ('9788418358789', 'Balada de los pájaros cantores y las serpientes', 'Los Juegos del Hambre', 4,
        'Orígenes de Coriolanus Snow en la décima edición de los juegos.', 4, 517, '2020-05-19',
        'https://m.media-amazon.com/images/I/71vnyzan0hL._UF1000,1000_QL80_.jpg'),
       ('9788498386264', 'El ladrón del rayo', 'Percy Jackson y los dioses del Olimpo', 1,
        'Percy descubre que es hijo de Poseidón y que el rayo de Zeus ha desaparecido.', 5, 416,
        '2005-07-01', 'https://imagessl6.casadellibro.com/a/l/s7/64/9788498386264.webp'),
       ('9788498386271', 'El mar de los monstruos', 'Percy Jackson y los dioses del Olimpo', 2,
        'Percy y Annabeth buscan el Vellocino de Oro para salvar el campamento.', 5, 279,
        '2006-04-01', 'https://imagessl6.casadellibro.com/a/l/s7/71/9788498386271.webp'),
       ('9788498386288', 'La maldición del Titán', 'Percy Jackson y los dioses del Olimpo', 3,
        'Una peligrosa misión rescata a Artemisa y enfrenta la traición del Titán.', 5, 312,
        '2007-05-01', 'https://imagessl6.casadellibro.com/a/l/s7/88/9788498386288.webp'),
       ('9788498386295', 'La batalla del laberinto', 'Percy Jackson y los dioses del Olimpo', 4,
        'Un laberinto infinito oculta la puerta que podría acabar con el Olimpo.', 5, 361,
        '2008-05-06', 'https://imagessl6.casadellibro.com/a/l/s7/95/9788498386295.webp'),
       ('9788498386301', 'El último héroe del Olimpo', 'Percy Jackson y los dioses del Olimpo', 5,
        'La guerra entre dioses y titanes llega a Manhattan.', 5, 381, '2009-05-05',
        'https://imagessl6.casadellibro.com/a/l/s7/01/9788498386301.webp'),
       ('9788418130497', 'La casa en el mar más azul', 'La casa en el mar cerúleo', 1,
        'Linus investiga un orfanato mágico en una isla remota y encuentra una familia improbable.', 6, 396,
        '2020-03-17', 'https://m.media-amazon.com/images/I/81hFGZkf+UL._AC_UF1000,1000_QL80_.jpg'),
       ('9788419320847', 'En algún lugar del mar más azul', 'La casa en el mar cerúleo', 2,
        'Arthur y los niños del orfanato enfrentan nuevas amenazas políticas y mágicas.', 6, 448,
        '2024-09-10', 'https://m.media-amazon.com/images/I/811q8fSXioL._UF1000,1000_QL80_.jpg'),
       ('9788416840658', 'La canción de Aquiles', NULL, NULL,
        'Patroclo narra su vida junto a Aquiles hasta la guerra de Troya.', 7, 416, '2011-09-20',
        'https://m.media-amazon.com/images/I/71jc29-6iVL._AC_UF1000,1000_QL80_.jpg'),
       ('9788408139637', 'Ciudad de hueso', 'Cazadores de sombras: Instrumentos mortales', 1,
        'Clary descubre el mundo de los cazadores de sombras en Nueva York.', 8, 485, '2007-03-27',
        'https://m.media-amazon.com/images/I/81SqvsSRQUL.jpg'),
       ('9788408139644', 'Ciudad de ceniza', 'Cazadores de sombras: Instrumentos mortales', 2,
        'Valentine amenaza con desatar demonios sobre los nefilim.', 8, 453, '2008-03-25',
        'https://proassetspdlcom.cdnstatics2.com/usuaris/libros/fotos/5/original/9788408087533.jpg'),
       ('9788408139651', 'Ciudad de cristal', 'Cazadores de sombras: Instrumentos mortales', 3,
        'La Ciudad de Cristal de Alicante se convierte en campo de batalla.', 8, 541, '2009-03-24',
        'https://proassetspdlcom.cdnstatics2.com/usuaris/libros/fotos/6/original/5018_1_Ciudaddecristal.jpg'),
       ('9788408008545', 'Ciudad de los ángeles caídos', 'Cazadores de sombras: Instrumentos mortales', 4,
        'Amores prohibidos y nuevas alianzas tras la guerra contra Valentine.', 8, 425, '2011-04-05',
        'https://m.media-amazon.com/images/I/91lXIPaM9-L._AC_UF1000,1000_QL80_.jpg'),
       ('9788408008552', 'Ciudad de las almas perdidas', 'Cazadores de sombras: Instrumentos mortales', 5,
        'Jace y Clary libran una batalla contra la posesión y la magia oscura.', 8, 535, '2012-05-08',
        'https://static.wikia.nocookie.net/shadowhunters/images/7/7e/CDS5_portada_ES_01.jpg/revision/latest?cb=20130103205258&path-prefix=es'),
       ('9788408156876', 'Ciudad del fuego celestial', 'Cazadores de sombras: Instrumentos mortales', 6,
        'El enfrentamiento final contra Sebastian y el caos en los mundos.', 8, 725, '2014-05-27',
        'https://static.wikia.nocookie.net/shadowhunters/images/3/3f/CDS6_portada_ES_01.jpg/revision/latest?cb=20200527232419&path-prefix=es');

INSERT INTO generos (nombre, descripcion)
VALUES
-- Ficción general
('Novela', 'Relato extenso de ficción con desarrollo complejo de personajes.'),
('Cuento', 'Narración breve de ficción con pocos personajes.'),
('Fabula', 'Historia breve con moraleja, frecuentemente protagonizada por animales.'),
('Leyenda', 'Relato tradicional que mezcla hechos reales y fantásticos.'),
('Mitologia', 'Conjunto de relatos basados en mitos de distintas culturas.'),

-- Géneros principales
('Ciencia Ficción', 'Explora el futuro, la tecnología y el espacio.'),
('Fantasía', 'Incluye magia, criaturas míticas y mundos imaginarios.'),
('Distopía', 'Sociedades futuras en crisis o bajo regímenes opresivos.'),
('Utopía', 'Representación de sociedades ideales.'),
('Realismo', 'Representación fiel de la vida cotidiana.'),
('Naturalismo', 'Corriente que enfatiza el determinismo biológico y social.'),

-- Géneros narrativos populares
('Romance', 'Historias centradas en relaciones amorosas.'),
('Drama', 'Conflictos emocionales y humanos intensos.'),
('Comedia', 'Relatos humorísticos con tono ligero.'),
('Tragedia', 'Historias con desenlaces desafortunados.'),
('Satira', 'Crítica social mediante humor e ironía.'),

-- Suspense y crimen
('Misterio', 'Enigmas o crímenes por resolver.'),
('Thriller', 'Narrativa de alta tensión y suspense.'),
('Terror', 'Diseñado para provocar miedo o inquietud.'),
('Policiaco', 'Investigaciones criminales y detectives.'),
('Novela Negra', 'Crimen con enfoque realista y tono oscuro.'),
('Espionaje', 'Historias de agentes secretos y conspiraciones.'),

-- Aventura y ambientación
('Aventura', 'Viajes, exploraciones y acción.'),
('Historico', 'Ambientado en épocas pasadas con contexto real.'),
('Western', 'Ambientado en el viejo oeste americano.'),
('Epico', 'Hazañas heroicas y eventos grandiosos.'),
('Gotico', 'Ambientes oscuros con elementos sobrenaturales.'),

-- Subgéneros modernos
('Cyberpunk', 'Alta tecnología en sociedades decadentes.'),
('Steampunk', 'Tecnología a vapor en mundos alternativos.'),
('Realismo Magico', 'Elementos mágicos dentro de entornos realistas.'),
('Experimental', 'Rompe estructuras narrativas tradicionales.'),
('Narrativa Contemporanea', 'Ambientada en la actualidad.'),

-- Formatos y públicos
('Poesia', 'Expresión literaria en verso.'),
('Ensayo', 'Reflexión subjetiva sobre un tema.'),
('Biografia', 'Vida de una persona narrada por otro.'),
('Autobiografia', 'Vida narrada por uno mismo.'),
('Infantil', 'Dirigido a niños.'),
('Juvenil', 'Dirigido a adolescentes.'),
('Novela Grafica', 'Narrativa visual con ilustraciones.'),
('Comic', 'Historias contadas en viñetas.'),

-- Otros enfoques
('Filosofia', 'Explora ideas sobre existencia y conocimiento.'),
('Autoayuda', 'Desarrollo personal y crecimiento emocional.'),
('Divulgacion Cientifica', 'Explica la ciencia al público general.'),
('Costumbrista', 'Describe costumbres y tradiciones sociales.'),
('Literatura LGBT+', 'Explora diversidad sexual y de género.');

INSERT INTO libros_generos (libro_id, genero_id)
VALUES
-- El Rey Oscuro / El heredero oscuro
(1, 2), (1, 23), (1, 26), (1, 39),
(2, 2), (2, 23), (2, 26), (2, 39),

-- Project Hail Mary
(3, 6), (3, 23), (3, 1),

-- Babel
(4, 2), (4, 24), (4, 12), (4, 43),

-- Los Juegos del Hambre (trilogía + precuela)
(5, 8), (5, 23), (5, 12), (5, 39),
(6, 8), (6, 23), (6, 12), (6, 39),
(7, 8), (7, 23), (7, 12), (7, 39),
(8, 8), (8, 23), (8, 24), (8, 39),

-- Percy Jackson
(9, 2), (9, 5), (9, 23), (9, 39),
(10, 2), (10, 5), (10, 23), (10, 39),
(11, 2), (11, 5), (11, 23), (11, 39),
(12, 2), (12, 5), (12, 23), (12, 39),
(13, 2), (13, 5), (13, 23), (13, 39),

-- La casa en el mar cerúleo
(14, 2), (14, 12), (14, 14), (14, 39),
(15, 2), (15, 12), (15, 14), (15, 39),

-- La canción de Aquiles
(16, 2), (16, 5), (16, 12), (16, 15),

-- Cazadores de sombras
(17, 2), (17, 12), (17, 23), (17, 39),
(18, 2), (18, 12), (18, 23), (18, 39),
(19, 2), (19, 12), (19, 23), (19, 39),
(20, 2), (20, 12), (20, 23), (20, 39),
(21, 2), (21, 12), (21, 23), (21, 39),
(22, 2), (22, 12), (22, 23), (22, 39);

INSERT INTO tropos (nombre, descripcion)
VALUES
-- Tropos clásicos
('Viaje en el tiempo', 'Personajes que se desplazan entre pasado y futuro.'),
('Bucle temporal', 'Eventos que se repiten una y otra vez.'),
('Destino inevitable', 'Los personajes no pueden escapar de su destino.'),
('Elegido', 'Un personaje está destinado a cumplir una misión especial.'),
('Profecia', 'Un evento predicho que guía la historia.'),

-- Relaciones y romance
('Solo hay una cama', 'Dos personajes deben compartir cama generando tensión.'),
('Enemigos a amantes', 'Dos personajes pasan del conflicto al romance.'),
('Amigos a amantes', 'Una amistad evoluciona en relación romántica.'),
('Amor prohibido', 'Relación que no está permitida por normas sociales o externas.'),
('Triangulo amoroso', 'Tres personajes involucrados románticamente.'),
('Relación falsa', 'Una pareja finge estar junta y termina siendo real.'),
('Reencuentro', 'Personajes que se separaron vuelven a encontrarse.'),
('Amor a primera vista', 'Atracción instantánea entre personajes.'),

-- Identidad y secretos
('Identidad secreta', 'Un personaje oculta quién es realmente.'),
('Doble vida', 'Un personaje mantiene dos identidades distintas.'),
('Amnesia', 'Un personaje pierde sus recuerdos.'),
('El impostor', 'Alguien suplanta la identidad de otro.'),
('Secreto revelado', 'Un secreto cambia el rumbo de la historia.'),

-- Conflicto y narrativa
('Cuenta regresiva', 'El tiempo limitado genera tensión.'),
('Giro inesperado', 'Cambio brusco en la trama.'),
('Narrador no fiable', 'El narrador no dice toda la verdad.'),
('Flashback', 'Saltos al pasado para explicar eventos.'),
('Foreshadowing', 'Pistas sobre eventos futuros.'),
('Final abierto', 'La historia no tiene un cierre definitivo.'),

-- Acción y aventura
('Entrenamiento', 'El protagonista mejora sus habilidades.'),
('Venganza', 'Motivación basada en ajustar cuentas.'),
('Redencion', 'Un personaje busca perdón por acciones pasadas.'),
('Sacrificio heroico', 'Un personaje se sacrifica por otros.'),
('Ultima batalla', 'Enfrentamiento final decisivo.'),

-- Fantasía y sci-fi
('Magia', 'Existencia de poderes sobrenaturales.'),
('Sistema de magia', 'Reglas definidas para el uso de magia.'),
('Mundo paralelo', 'Universos alternativos coexistiendo.'),
('Portal', 'Acceso entre mundos distintos.'),
('Distopia', 'Sociedad opresiva o en decadencia.'),
('Apocalipsis', 'Fin del mundo o colapso global.'),
('Inteligencia artificial rebelde', 'Máquinas que se vuelven contra humanos.'),
('Aliens', 'Presencia de vida extraterrestre.'),

-- Personajes arquetípicos
('Mentor', 'Guía que ayuda al protagonista.'),
('Antiheroe', 'Protagonista con rasgos moralmente ambiguos.'),
('Villano carismatico', 'Antagonista con encanto o atractivo.'),
('Damisela en apuros', 'Personaje que necesita ser rescatado.'),
('Equipo improbable', 'Grupo de personajes muy distintos que colaboran.'),
('El alivio comico', 'Personaje que aporta humor.'),

-- Situaciones comunes
('Malentendido', 'Conflicto causado por falta de comunicación.'),
('Coincidencia improbable', 'Eventos que ocurren por casualidad extrema.'),
('De cero a heroe', 'Ascenso de personaje común a extraordinario.'),
('Pez fuera del agua', 'Personaje fuera de su entorno habitual.'),
('Rivales', 'Competencia constante entre personajes.'),

-- Horror y tensión
('Casa embrujada', 'Lugar habitado por entidades sobrenaturales.'),
('Entidad desconocida', 'Amenaza incomprensible o invisible.'),
('Supervivencia', 'Personajes luchan por mantenerse con vida.'),
('Paranoia', 'Desconfianza extrema entre personajes.'),

-- Sociales y psicológicos
('Vigilancia', 'Control constante sobre la población.'),
('Control mental', 'Manipulación de la mente de otros.'),
('Experimento fallido', 'Consecuencias de pruebas que salen mal.'),
('Corrupcion', 'Degradación moral de personajes o sistemas.'),
('Sistema opresivo', 'Estructura social injusta.'),

-- Otros populares
('Romper la cuarta pared', 'Personaje consciente de ser ficción.'),
('Metaficcion', 'La obra reconoce su propia naturaleza ficticia.'),
('Objeto de poder', 'Elemento con habilidades especiales.'),
('MacGuffin', 'Objeto que impulsa la trama sin importar su naturaleza.'),
('Cuenta con los dedos', 'Recurso visual para generar tensión o conteo.'),
('Final feliz', 'Resolución positiva para los personajes.');

INSERT INTO libros_tropos (libro_id, tropo_id)
VALUES
-- El Rey Oscuro / secuela
(1, 4), (1, 30), (1, 14), (1, 24),
(2, 4), (2, 30), (2, 17), (2, 25),

-- Project Hail Mary
(3, 17), (3, 20), (3, 33), (3, 37),

-- Babel
(4, 30), (4, 54), (4, 18), (4, 23),

-- Los Juegos del Hambre
(5, 54), (5, 25), (5, 27), (5, 46),
(6, 54), (6, 25), (6, 22), (6, 45),
(7, 54), (7, 27), (7, 22), (7, 49),
(8, 4), (8, 25), (8, 38), (8, 45),

-- Percy Jackson
(9, 4), (9, 31), (9, 37), (9, 44),
(10, 4), (10, 24), (10, 38), (10, 45),
(11, 4), (11, 25), (11, 44), (11, 49),
(12, 4), (12, 37), (12, 44), (12, 45),
(13, 4), (13, 27), (13, 49), (13, 44),

-- Cerúleo
(14, 44), (14, 40), (14, 48), (14, 60),
(15, 44), (15, 54), (15, 48), (15, 25),

-- Aquiles
(16, 4), (16, 9), (16, 27), (16, 45),

-- Shadowhunters
(17, 14), (17, 7), (17, 30), (17, 44),
(18, 14), (18, 8), (18, 30), (18, 25),
(19, 14), (19, 27), (19, 44), (19, 49),
(20, 14), (20, 9), (20, 30), (20, 44),
(21, 14), (21, 17), (21, 30), (21, 25),
(22, 14), (22, 27), (22, 49), (22, 45);

INSERT INTO bibliotecas (usuario_id, nombre)
VALUES (1, 'Leyendo'), (1, 'Leído'), (1, 'Por Leer'),
       (2, 'Leyendo'), (2, 'Leído'), (2, 'Por Leer'),
       (3, 'Leyendo'), (3, 'Leído'), (3, 'Por Leer'),
       (4, 'Leyendo'), (4, 'Leído'), (4, 'Por Leer'),
       (5, 'Leyendo'), (5, 'Leído'), (5, 'Por Leer'),
       (6, 'Leyendo'), (6, 'Leído'), (6, 'Por Leer'),
       (7, 'Leyendo'), (7, 'Leído'), (7, 'Por Leer'),
       (8, 'Leyendo'), (8, 'Leído'), (8, 'Por Leer'),
       (9, 'Leyendo'), (9, 'Leído'), (9, 'Por Leer'),
       (10, 'Leyendo'), (10, 'Leído'), (10, 'Por Leer'),
       (11, 'Leyendo'), (11, 'Leído'), (11, 'Por Leer'),
       (12, 'Leyendo'), (12, 'Leído'), (12, 'Por Leer');

INSERT INTO usuarios_libros (usuario_id, libro_id, estado, pagina_actual, fecha_inicio, fecha_fin, calificacion,
                             fecha_actualizacion)
VALUES (1, 1, 'leyendo', 210, '2026-02-01', NULL, NULL, '2026-04-10 11:00:00'),
       (1, 5, 'leyendo', 180, '2026-04-01', NULL, NULL, '2026-04-12 09:30:00'),
       (1, 14, 'leido', 396, '2025-11-01', '2025-11-20', 5, NULL),
       (1, 16, 'leido', 416, '2025-08-10', '2025-08-18', 5, NULL),
       (2, 2, 'quiero_leer', NULL, NULL, NULL, NULL, NULL),
       (2, 6, 'leyendo', 90, '2026-04-05', NULL, NULL, '2026-04-11 18:00:00'),
       (2, 17, 'leido', 485, '2025-06-01', '2025-06-14', 4, NULL),
       (3, 3, 'leido', 496, '2026-01-15', '2026-02-15', 5, NULL),
       (3, 10, 'leyendo', 120, '2026-03-20', NULL, NULL, NULL),
       (3, 21, 'quiero_leer', NULL, NULL, NULL, NULL, NULL),
       (4, 4, 'leyendo', 200, '2026-04-02', NULL, NULL, NULL),
       (4, 9, 'leido', 416, '2025-01-10', '2025-01-25', 5, NULL),
       (4, 11, 'leido', 279, '2025-02-01', '2025-02-08', 4, NULL),
       (5, 12, 'leyendo', 200, '2026-04-08', NULL, NULL, NULL),
       (5, 18, 'leido', 453, '2024-12-01', '2024-12-20', 5, NULL),
       (6, 13, 'leido', 381, '2024-07-01', '2024-07-12', 5, NULL),
       (6, 16, 'leido', 416, '2023-05-01', '2023-05-06', 5, NULL),
       (6, 15, 'quiero_leer', NULL, NULL, NULL, NULL, NULL),
       (7, 9, 'leido', 416, '2022-06-01', '2022-06-10', 5, NULL),
       (7, 10, 'leido', 279, '2022-07-01', '2022-07-05', 5, NULL),
       (7, 11, 'leido', 312, '2022-08-01', '2022-08-09', 5, NULL),
       (8, 7, 'leido', 390, '2021-03-01', '2021-03-15', 5, NULL),
       (8, 8, 'leyendo', 340, '2026-03-15', NULL, NULL, NULL),
       (8, 5, 'leido', 374, '2020-01-01', '2020-01-08', 5, NULL),
       (9, 17, 'leido', 485, '2019-04-01', '2019-04-12', 5, NULL),
       (9, 22, 'leyendo', 400, '2026-04-01', NULL, NULL, NULL),
       (9, 19, 'leido', 541, '2020-02-01', '2020-02-22', 5, NULL),
       (10, 3, 'quiero_leer', NULL, NULL, NULL, NULL, NULL),
       (10, 20, 'leido', 425, '2024-09-01', '2024-09-18', 4, NULL),
       (10, 6, 'abandonado', 80, '2025-11-01', NULL, NULL, NULL),
       (11, 16, 'leido', 416, '2024-01-01', '2024-01-07', 5, NULL),
       (11, 1, 'quiero_leer', NULL, NULL, NULL, NULL, NULL),
       (12, 14, 'leido', 396, '2025-05-01', '2025-05-14', 5, NULL),
       (12, 15, 'leyendo', 120, '2026-04-10', NULL, NULL, NULL),
       (12, 8, 'quiero_leer', NULL, NULL, NULL, NULL, NULL);

INSERT INTO resenas (usuario_id, libro_id, titulo, contenido, calificacion, contiene_spoiler)
VALUES (1, 1, 'Oscura y elegante', 'Atmósfera gótica impecable; no puedo soltarlo.', 5, FALSE),
       (2, 2, 'Sube el listón', 'Más acción y política que el primero.', 4, FALSE),
       (3, 3, 'Adictivo', 'Ciencia en primera persona que engancha.', 5, FALSE),
       (4, 4, 'Incómodo (en el buen sentido)', 'Te obliga a pensar en el colonialismo.', 5, TRUE),
       (8, 5, 'Katniss icónica', 'Arranque perfecto de la saga.', 5, FALSE),
       (8, 7, 'Final contundente', 'Cierra arcos con fuerza; me dejó KO.', 5, TRUE),
       (2, 6, 'Tensión política', 'El vasallaje eleva la historia.', 4, FALSE),
       (7, 9, 'Puerta de entrada ideal', 'Humor y mitología balanceados.', 5, FALSE),
       (7, 13, 'Epico', 'La batalla por Nueva York es cinematográfica.', 5, TRUE),
       (4, 10, 'Viaje por el mar', 'Annabeth y Percy funcionan genial.', 4, FALSE),
       (1, 14, 'Abrazo literario', 'Found family que necesitaba leer.', 5, FALSE),
       (12, 14, 'Tierna sin ser cursi', 'Klune en su mejor tono.', 5, FALSE),
       (12, 15, 'Más madura', 'Sigue doliendo en los puntos correctos.', 4, FALSE),
       (6, 16, 'Lloré', 'Miller redefine la Ilíada.', 5, TRUE),
       (11, 16, 'Obra maestra lírica', 'Cada frase está medida.', 5, FALSE),
       (9, 17, 'Mi entrada a Shadowhunters', 'Clary y Jace arrasan.', 5, FALSE),
       (9, 22, 'Cierre de saga', 'Se nota el cansancio pero cumple.', 4, TRUE),
       (5, 18, ' Valentine sigue dando miedo', 'Mejor que la primera en ritmo.', 4, FALSE),
       (10, 20, 'Giro interesante', 'Explora límites éticos.', 4, TRUE),
       (1, 8, 'Snow humanizado', 'Prequel incómodo y necesario.', 4, TRUE),
       (3, 11, 'Titanes al poder', 'El mejor cliffhanger de la serie PJ.', 5, TRUE),
       (6, 12, 'Laberinto claustrofóbico', 'Me mantuvo en tensión.', 5, FALSE),
       (8, 8, 'Coriolanus fascina', 'Mezcla de glamour y crueldad.', 4, FALSE),
       (2, 3, 'Grace memorable', 'Personajes secundarios brillantes.', 5, FALSE),
       (5, 19, 'Ciudad de Cristal épica', 'Batallas al estilo Clare.', 5, TRUE);

INSERT INTO comentarios (resena_id, usuario_id, contenido, contiene_spoiler)
VALUES (1, 2, 'Totalmente: la ambientación es clavada.', FALSE),
       (1, 4, '¿Ya leíste la segunda?', FALSE),
       (4, 1, 'El final me dejó pensando días.', TRUE),
       (5, 7, 'District 12 forever.', FALSE),
       (8, 5, 'Yo empecé por la peli y el libro gana.', FALSE),
       (11, 12, 'Arthur es tesoro nacional.', FALSE),
       (14, 11, 'Las últimas páginas destrozan.', TRUE),
       (14, 1, 'Miller no perdona.', TRUE),
       (16, 10, 'Me pasó lo mismo con el primer tomo.', FALSE),
       (17, 8, '650 páginas que se hacen largas pero vale.', TRUE),
       (18, 9, 'Valentine es el mejor villano de Clare.', FALSE),
       (21, 7, 'El titán Kronos 👀', TRUE),
       (22, 4, 'El laberinto da vértigo.', FALSE),
       (13, 6, 'Arthur sigue siendo mi favorito.', FALSE),
       (19, 3, 'Coincido en lo ético del giro.', TRUE),
       (24, 6, 'Rocky es lo mejor de PHM.', FALSE),
       (2, 3, 'La segunda parte siempre cuesta.', FALSE),
       (6, 1, 'El Capitolio me horroriza.', FALSE),
       (10, 8, 'Quiero más Percabeth.', FALSE),
       (15, 12, 'Relectura obligada cada año.', FALSE),
       (20, 11, 'El tema posesión está muy bien llevado.', TRUE),
       (23, 2, 'Snow adolescente da escalofríos.', TRUE),
       (7, 10, 'Los tributos mayores dan juego.', FALSE),
       (12, 1, 'Lucy es maravilloso.', FALSE),
       (9, 4, 'Camp Half-Blood goals.', FALSE),
       (25, 8, 'Los portales en Alicante 💀', TRUE),
       (16, 12, 'Magnus aparece poco pero bien.', FALSE),
       (3, 11, 'Grace es ciencia hard light.', FALSE),
       (5, 9, 'Peeta o Gale ya sabes…', TRUE),
       (8, 10, 'Rick sabe escribir mitología.', FALSE),
       (17, 5, 'El epílogo cerró mi adolescencia literaria.', TRUE),
       (18, 7, 'Jonathan 😭', TRUE),
       (21, 12, 'Annabeth siempre planifica bien.', FALSE),
       (11, 9, 'Los niños tienen personalidad propia.', FALSE),
       (22, 9, 'Daedalus da miedo.', FALSE),
       (13, 11, 'Esperando la tercera si Klune anima.', FALSE);

INSERT INTO actividad_usuario (usuario_actor_id, usuario_destino_id, tipo, libro_id, resena_id, texto, fecha_creacion)
VALUES (1, NULL, 'RESENA', 1, 1, 'Nueva reseña: Oscura y elegante', '2026-04-01 10:00:00'),
       (2, NULL, 'RESENA', 2, 2, 'Nueva reseña: Sube el listón', '2026-04-01 11:05:00'),
       (3, NULL, 'RESENA', 3, 3, 'Nueva reseña: Adictivo', '2026-04-02 09:00:00'),
       (8, NULL, 'RESENA', 5, 5, 'Nueva reseña: Katniss icónica', '2026-04-02 14:20:00'),
       (7, NULL, 'RESENA', 9, 8, 'Nueva reseña: Puerta de entrada ideal', '2026-04-03 08:15:00'),
       (1, NULL, 'RESENA', 14, 11, 'Nueva reseña: Abrazo literario', '2026-04-03 16:40:00'),
       (6, NULL, 'RESENA', 16, 14, 'Nueva reseña: Lloré', '2026-04-04 12:00:00'),
       (9, NULL, 'RESENA', 17, 16, 'Nueva reseña: Mi entrada a Shadowhunters', '2026-04-04 18:30:00'),
       (1, NULL, 'PROGRESO', 1, NULL, 'Progreso: pág. 210 · leyendo', '2026-04-05 08:00:00'),
       (8, NULL, 'PROGRESO', 5, NULL, 'Progreso: releyendo HG · leyendo', '2026-04-05 09:10:00'),
       (4, NULL, 'PROGRESO', 4, NULL, 'Progreso: pág. 200 · Babel', '2026-04-06 07:45:00'),
       (9, NULL, 'PROGRESO', 22, NULL, 'Progreso: Ciudad del fuego celestial', '2026-04-06 19:00:00'),
       (12, NULL, 'PROGRESO', 15, NULL, 'Progreso: segunda parte Klune', '2026-04-07 21:00:00'),
       (2, 1, 'COMENTARIO', 1, 1, 'Totalmente: la ambientación es clavada.', '2026-04-07 10:00:00'),
       (4, 1, 'COMENTARIO', 1, 1, '¿Ya leíste la segunda?', '2026-04-07 11:20:00'),
       (5, 7, 'COMENTARIO', 9, 8, 'Yo empecé por la peli y el libro gana.', '2026-04-08 09:00:00'),
       (11, 6, 'COMENTARIO', 16, 14, 'Las últimas páginas destrozan.', '2026-04-08 13:30:00'),
       (10, 9, 'COMENTARIO', 17, 16, 'Me pasó lo mismo con el primer tomo.', '2026-04-09 08:50:00'),
       (3, NULL, 'RESENA', 11, 21, 'Nueva reseña: Titanes al poder', '2026-04-09 15:00:00'),
       (8, NULL, 'RESENA', 8, 23, 'Nueva reseña: Coriolanus fascina', '2026-04-09 17:10:00'),
       (6, NULL, 'RESENA', 12, 22, 'Nueva reseña: Laberinto claustrofóbico', '2026-04-10 11:00:00'),
       (7, NULL, 'RESENA', 13, 9, 'Nueva reseña: Epico', '2026-04-10 12:30:00'),
       (5, NULL, 'RESENA', 18, 18, 'Nueva reseña: Valentine sigue dando miedo', '2026-04-10 14:00:00'),
       (11, NULL, 'RESENA', 16, 15, 'Nueva reseña: Obra maestra lírica', '2026-04-11 09:00:00'),
       (12, NULL, 'RESENA', 15, 13, 'Nueva reseña: Más madura', '2026-04-11 10:15:00'),
       (10, NULL, 'RESENA', 20, 19, 'Nueva reseña: Giro interesante', '2026-04-11 16:45:00'),
       (4, NULL, 'RESENA', 4, 4, 'Nueva reseña: Incómodo (en el buen sentido)', '2026-04-12 08:00:00'),
       (1, NULL, 'PROGRESO', 5, NULL, 'Progreso: pág. 180 · Los juegos del hambre', '2026-04-12 09:30:00'),
       (2, NULL, 'PROGRESO', 6, NULL, 'Progreso: En llamas avanza fuerte', '2026-04-12 18:00:00'),
       (9, NULL, 'PROGRESO', 22, NULL, 'Actualización: mitad del tomo final MI', '2026-04-13 07:30:00'),
       (6, 6, 'COMENTARIO', 16, 14, 'Miller no perdona.', '2026-04-13 12:00:00'),
       (8, 8, 'COMENTARIO', 5, 5, 'District 12 forever.', '2026-04-13 14:00:00'),
       (3, 2, 'COMENTARIO', 3, 24, 'Grace es ciencia hard light.', '2026-04-13 16:20:00'),
       (7, 3, 'COMENTARIO', 11, 21, 'El titán Kronos 👀', '2026-04-14 09:00:00'),
       (5, 6, 'COMENTARIO', 12, 22, 'El laberinto da vértigo.', '2026-04-14 11:30:00');

INSERT INTO likes_resenas (usuario_id, resena_id)
VALUES (2, 1), (3, 1), (4, 1),
       (3, 2), (5, 2),
       (1, 3), (11, 3), (12, 3),
       (1, 4), (8, 4),
       (7, 5), (10, 5),
       (4, 6), (9, 6),
       (10, 7), (11, 7),
       (5, 8), (6, 8), (12, 8),
       (12, 9), (8, 9),
       (2, 10), (7, 10),
       (3, 11), (9, 11), (12, 11),
       (1, 12), (11, 12),
       (1, 13), (6, 13),
       (11, 14), (12, 14),
       (7, 15), (8, 15),
       (10, 16), (11, 16),
       (8, 17), (12, 17),
       (9, 18), (10, 18),
       (6, 19), (11, 19),
       (2, 20), (7, 20),
       (9, 21), (12, 21),
       (4, 22), (8, 22),
       (5, 23), (10, 23),
       (6, 24), (9, 24),
       (3, 25), (7, 25);

INSERT INTO usuarios_seguidores (seguidor_id, seguido_id)
VALUES (1, 2), (1, 4), (1, 7),
       (2, 3), (2, 8), (2, 11),
       (3, 1), (3, 9),
       (4, 5), (4, 12),
       (5, 6), (5, 10),
       (6, 7), (6, 11),
       (7, 8), (7, 9),
       (8, 1), (8, 10),
       (9, 11), (9, 12),
       (10, 4), (10, 7),
       (11, 3), (11, 6),
       (12, 1), (12, 5),
       (4, 1), (6, 4), (9, 6);

INSERT INTO bibliotecas_libros (biblioteca_id, libro_id)
VALUES
       (1, 1), (1, 5),
       (2, 14), (2, 16), (2, 17),
       (3, 8), (3, 15), (3, 21),
       (4, 6),
       (5, 17), (5, 18),
       (6, 4), (6, 22),
       (7, 12), (7, 13),
       (8, 13),
       (10, 11),
       (11, 9), (11, 10),
       (14, 19),
       (15, 7), (15, 8),
       (16, 20),
       (17, 22),
       (19, 17), (19, 18), (19, 19),
       (20, 21),
       (23, 14),
       (24, 15),
       (25, 16),
       (26, 4),
       (28, 3),
       (29, 10),
       (31, 12),
       (32, 18),
       (34, 2),
       (35, 8),
       (36, 15),
       (2, 3), (2, 5), (2, 7),
       (5, 4), (8, 20),
       (11, 22),
       (14, 6),
       (17, 5),
       (20, 9),
       (23, 11),
       (26, 12),
       (29, 13),
       (32, 21),
       (35, 1),
       (3, 6), (3, 10),
       (6, 11),
       (9, 8),
       (12, 19),
       (15, 22),
       (18, 7),
       (21, 14),
       (24, 16),
       (27, 17),
       (30, 18),
       (33, 20),
       (36, 4);
