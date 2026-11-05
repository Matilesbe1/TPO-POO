package modelo.reclamos;

import java.io.Serializable;
import java.time.LocalDateTime;

public final class HistorialEstado implements Serializable {
    private final EstadoReclamo estado;
    private final String observacion;
    private final LocalDateTime fecha;

    public HistorialEstado(EstadoReclamo estado, String observacion, LocalDateTime fecha) {
        if (estado == null || fecha == null) throw new IllegalArgumentException("Estado y fecha son obligatorios.");
        this.estado = estado;
        this.observacion = observacion == null ? "" : observacion;
        this.fecha = fecha;
    }
    public EstadoReclamo getEstado() { return estado; }
    public String getObservacion() { return observacion; }
    public LocalDateTime getFecha() { return fecha; }
}
