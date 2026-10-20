package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.List;

/** Imágenes de un apartamento en un servicio externo (7.3): mínimo 1, máximo 10, con una principal. */
public record GaleriaImagenes(List<String> urls, int indicePrincipal) {

    public static final int MINIMO = 1;
    public static final int MAXIMO = 10;

    public GaleriaImagenes {
        if (urls == null || urls.size() < MINIMO || urls.size() > MAXIMO) {
            throw new ReglaDominioException("El apartamento debe tener entre 1 y 10 imágenes.");
        }
        if (urls.stream().anyMatch(u -> u == null || u.isBlank())) {
            throw new ReglaDominioException("Las imágenes deben tener una dirección válida.");
        }
        if (indicePrincipal < 0 || indicePrincipal >= urls.size()) {
            throw new ReglaDominioException("Debe indicarse una imagen principal dentro de la galería.");
        }
        urls = List.copyOf(urls);
    }

    public String principal() {
        return urls.get(indicePrincipal);
    }
}
