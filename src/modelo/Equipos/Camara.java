package modelo.equipos;

import java.time.LocalDate;
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

    @Override public Prioridad calcularPrioridadFalla(int fallasEnSector) {
        return Prioridad.ALTA;
    }
    public String getResolucion() { return resolucion; }
    public int getDiasGrabacion() { return diasGrabacion; }
}
