package modelo.expensas;

import modelo.personas.Vecino;
import java.io.Serializable;
import java.time.LocalDate;

// Clase inmutable: notificación formal al titular de un lote moroso (RN02.6).
public final class Notificacion implements Serializable {
    private final Vecino destinatario;
    private final LocalDate fecha;
    private final String mensaje;

    public Notificacion(Vecino destinatario, LocalDate fecha, String mensaje) {
        if (destinatario == null || fecha == null || mensaje == null || mensaje.isBlank())
            throw new IllegalArgumentException("Destinatario, fecha y mensaje son obligatorios.");
        this.destinatario = destinatario;
        this.fecha = fecha;
        this.mensaje = mensaje;
    }
    public Vecino getDestinatario() { return destinatario; }
    public LocalDate getFecha() { return fecha; }
    public String getMensaje() { return mensaje; }
}
