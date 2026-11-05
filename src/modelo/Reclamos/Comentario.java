package modelo;

import java.time.LocalDateTime;

public class Comentario {
    private final String autor;
    private final String texto;
    private final LocalDateTime fechaHora;

    public Comentario(String autor, String texto) {
        this.autor = autor;
        this.texto = texto;
        this.fechaHora = LocalDateTime.now();
    }

    public String getAutor() {
        return autor;
    }

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}