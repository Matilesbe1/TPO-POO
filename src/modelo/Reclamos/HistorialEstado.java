package modelo;

import java.time.LocalDateTime;

public class HistorialEstado {
    private final EstadoReclamo estado;
    private final String observacion;
    private final LocalDateTime fechaHora;

    public HistorialEstado(EstadoReclamo estado, String observacion) {
        this.estado = estado;
        this.observacion = observacion;
        this.fechaHora = LocalDateTime.now(); // Guarda la fecha y hora actual automáticamente
    }

    public EstadoReclamo getEstado() {
        return estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}