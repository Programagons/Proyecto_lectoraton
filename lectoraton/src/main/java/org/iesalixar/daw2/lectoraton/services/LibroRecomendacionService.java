package org.iesalixar.daw2.lectoraton.services;

import org.iesalixar.daw2.lectoraton.dtos.LibroMiniDTO;
import org.iesalixar.daw2.lectoraton.dtos.LibroRecomendacionDTO;
import org.iesalixar.daw2.lectoraton.entities.Libro;
import org.iesalixar.daw2.lectoraton.repositories.LibroRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LibroRecomendacionService {

    private final LibroRepository libroRepository;

    public LibroRecomendacionService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    /**
     * Obtiene una recomendación de libro.
     * @param libroId ID del libro.
     * @return LibroRecomendacionDTO.
     */
    public LibroRecomendacionDTO recomendar(long libroId) {
        Libro origen = libroRepository.findOneWithAssociations(libroId)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado."));

        LibroRecomendacionDTO dto = new LibroRecomendacionDTO();
        dto.setOrigen(toMini(origen));

        List<Libro> candidatos = listarSimilaresCandidatos(origen);
        if (!candidatos.isEmpty()) {
            Libro elegido = elegirMejorMatch(origen, candidatos);
            dto.setRecomendado(toMini(elegido));
            dto.setExplicacion(explicacionMatch(origen, elegido));
            return dto;
        }

        Libro mismoAutor = primerOtroDelAutor(origen);
        if (mismoAutor != null) {
            dto.setRecomendado(toMini(mismoAutor));
            dto.setExplicacion("No hay coincidencias por género ni tropo; te sugerimos otra obra del mismo autor.");
            return dto;
        }

        dto.setRecomendado(null);
        dto.setExplicacion("No hay otro libro en el catálogo al que relacionar este título.");
        return dto;
    }

    /**
     * Obtiene los libros similares candidatos.
     * @param libro Libro.
     * @return Lista de Libro.
     */
    private List<Libro> listarSimilaresCandidatos(Libro libro) {
        List<Long> generoIds = libro.getGeneros().stream().map(g -> g.getId()).toList();
        List<Long> tropoIds = libro.getTropos().stream().map(t -> t.getId()).toList();
        if (generoIds.isEmpty() && tropoIds.isEmpty()) {
            return List.of();
        }

        List<Libro> similares = new ArrayList<>();
        if (!generoIds.isEmpty() && !tropoIds.isEmpty()) {
            similares.addAll(libroRepository.findSimilares(libro.getId(), generoIds, tropoIds, PageRequest.of(0, 50)));
        } else if (!generoIds.isEmpty()) {
            similares.addAll(libroRepository.findAll(PageRequest.of(0, 80)).getContent().stream()
                    .filter(l -> !l.getId().equals(libro.getId()))
                    .filter(l -> l.getGeneros().stream().anyMatch(g -> generoIds.contains(g.getId())))
                    .collect(Collectors.toList()));
        } else {
            similares.addAll(libroRepository.findAll(PageRequest.of(0, 80)).getContent().stream()
                    .filter(l -> !l.getId().equals(libro.getId()))
                    .filter(l -> l.getTropos().stream().anyMatch(t -> tropoIds.contains(t.getId())))
                    .collect(Collectors.toList()));
        }

        return similares.stream()
                .filter(l -> !l.getId().equals(libro.getId()))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Elige el mejor match.
     * @param origen Libro origen.
     * @param candidatos Lista de Libro candidatos.
     * @return Libro.
     */
    private Libro elegirMejorMatch(Libro origen, List<Libro> candidatos) {
        Comparator<Libro> porTitulo = Comparator.comparing(l -> l.getTitulo() == null ? "" : l.getTitulo(),
                String.CASE_INSENSITIVE_ORDER);
        return candidatos.stream()
                .max(Comparator
                        .comparingInt((Libro c) -> puntuacion(origen, c))
                        .thenComparing(porTitulo))
                .orElse(candidatos.get(0));
    }

    /**
     * Calcula la puntuación.
     * @param origen Libro origen.
     * @param cand Libro candidato.
     * @return Puntuación.
     */
    private int puntuacion(Libro origen, Libro cand) {
        // Se calculan los géneros y tropos en común.
        Set<Long> og = idsGeneros(origen);
        Set<Long> ot = idsTropos(origen);
        int g = (int) cand.getGeneros().stream().filter(gx -> og.contains(gx.getId())).count();
        int t = (int) cand.getTropos().stream().filter(tx -> ot.contains(tx.getId())).count();
        int p = g * 10 + t * 8;

        // Se verifica si el autor es el mismo.
        Long aid = origen.getAutor() != null ? origen.getAutor().getId() : null;
        if (aid != null && cand.getAutor() != null && aid.equals(cand.getAutor().getId())) {
            p += 6;
        }
        // Se verifica si la saga es la misma.
        String saga = origen.getSagaNombre();
        if (saga != null && !saga.isBlank()
                && cand.getSagaNombre() != null
                && saga.equalsIgnoreCase(cand.getSagaNombre().trim())) {
            p += 5;
        }
        return p;
    }

    /**
     * Obtiene la explicación del match.
     * @param origen Libro origen.
     * @param elegido Libro elegido.
     * @return Explicación.
     */
    private String explicacionMatch(Libro origen, Libro elegido) {
        Set<Long> og = idsGeneros(origen);
        Set<Long> ot = idsTropos(origen);
        long gComun = elegido.getGeneros().stream().filter(g -> og.contains(g.getId())).count();
        long tComun = elegido.getTropos().stream().filter(t -> ot.contains(t.getId())).count();

        Long aid = origen.getAutor() != null ? origen.getAutor().getId() : null;
        boolean mismoAutor = aid != null && elegido.getAutor() != null && aid.equals(elegido.getAutor().getId());
        String saga = origen.getSagaNombre();
        boolean mismaSaga = saga != null && !saga.isBlank()
                && elegido.getSagaNombre() != null
                && saga.equalsIgnoreCase(elegido.getSagaNombre().trim());

        List<String> partes = new ArrayList<>();
        if (gComun > 0) {
            partes.add(gComun == 1 ? "1 género en común" : gComun + " géneros en común");
        }
        if (tComun > 0) {
            partes.add(tComun == 1 ? "1 tropo en común" : tComun + " tropos en común");
        }
        if (mismoAutor) {
            partes.add("mismo autor");
        }
        if (mismaSaga) {
            partes.add("la misma saga");
        }

        if (partes.isEmpty()) {
            return "Relacionado por el catálogo de parecidos.";
        }
        return "Por " + String.join(", ", partes) + ".";
    }

    /**
     * Obtiene el primer otro del autor.
     * @param libro Libro.
     * @return Libro.
     */
    private Libro primerOtroDelAutor(Libro libro) {
        if (libro.getAutor() == null) {
            return null;
        }
        return libroRepository.findByAutorId(libro.getAutor().getId(), PageRequest.of(0, 20)).getContent().stream()
                .filter(l -> !l.getId().equals(libro.getId()))
                .min(Comparator.comparing(l -> l.getTitulo() == null ? "" : l.getTitulo(), String.CASE_INSENSITIVE_ORDER))
                .orElse(null);
    }

    /**
     * Obtiene los IDs de los géneros.
     * @param l Libro.
     * @return Set de IDs de géneros.
     */
    private Set<Long> idsGeneros(Libro l) {
        return l.getGeneros().stream().map(g -> g.getId()).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Obtiene los IDs de los tropos.
     * @param l Libro.
     * @return Set de IDs de tropos.
     */
    private Set<Long> idsTropos(Libro l) {
        return l.getTropos().stream().map(t -> t.getId()).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Convierte un libro a un mini libro.
     * @param libro Libro.
     * @return LibroMiniDTO.
     */
    private LibroMiniDTO toMini(Libro libro) {
        LibroMiniDTO m = new LibroMiniDTO();
        m.setId(libro.getId());
        m.setTitulo(libro.getTitulo());
        m.setPortada(libro.getPortada());
        m.setAutorNombre(libro.getAutor() != null ? libro.getAutor().getNombreCompleto() : null);
        return m;
    }
}
