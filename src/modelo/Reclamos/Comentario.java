package modelo.reclamos;

import java.io.Serializable;
import java.time.LocalDateTime;

public final class Comentario implements Serializable {
    private final String autor;
    private final String texto;
    private final LocalDateTime fecha;

    public Comentario(String autor, String texto, LocalDateTime fecha) {
        if (autor == null || autor.isBlank() || texto == null || texto.isBlank() || fecha == null)
            throw new IllegalArgumentException("Autor, texto y fecha son obligatorios.");
        this.autor = autor;
        this.texto = texto;
        this.fecha = fecha;
    }
    public String getAutor() { return autor; }
    public String getTexto() { return texto; }
    public LocalDateTime getFecha() { return fecha; }
}
