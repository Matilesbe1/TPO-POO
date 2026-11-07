package modelo.equipos;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import modelo.reclamos.Prioridad;

public class Camara extends Equipo {
    private final String resolucion;
    private final int diasGrabacion;

    public Camara(String codigo, String sector, LocalDate instalacion,
                  String resolucion, int diasGrabacion) {
        super(codigo, sector, instalacion);
        if (resolucion == null || resolucion.isBlank() || diasGrabacion <= 0)
            throw new IllegalArgumentException("Resolución y días de grabación deben ser válidos.");
        this.resolucion = resolucion;
        this.diasGrabacion = diasGrabacion;
    }

    public static final int HORAS_MAXIMAS_SIN_FUNCIONAR = 48;   // RN03.5

    // RN03.2: una falla en una cámara siempre genera una orden de prioridad alta.
    @Override public Prioridad calcularPrioridadFalla(int fallasEnSector) {
        return Prioridad.ALTA;
    }
    @Override public String getTipo() { return "Cámara"; }

    // RN03.5: si una cámara lleva más de 48 horas sin funcionar, hay que informar al administrador.
    @Override public boolean requiereAvisoAdministrador(LocalDateTime fechaHora) {
        if (fechaHora == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        return getEstado() != EstadoEquipo.OPERATIVO && getFechaHoraFalla() != null
                && Duration.between(getFechaHoraFalla(), fechaHora).toMinutes() > HORAS_MAXIMAS_SIN_FUNCIONAR * 60;
    }
    public String getResolucion() { return resolucion; }
    public int getDiasGrabacion() { return diasGrabacion; }
}
