package modelo.personas;

import modelo.equipos.OrdenMantenimiento;
import modelo.excepciones.OrdenIncompletaException;
import java.io.Serializable;
import java.time.LocalDate;

public class EmpleadoMantenimiento implements Serializable {
    private final String legajo;
    private final String nombre;
    public EmpleadoMantenimiento(String legajo, String nombre) {
        if (legajo == null || legajo.isBlank() || nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Legajo y nombre son obligatorios.");
        this.legajo = legajo; this.nombre = nombre;
    }
    public String getLegajo() { return legajo; }
    public String getNombre() { return nombre; }

    // RF04: el empleado registra la reparación y cierra la orden quedando como técnico responsable.
    public void registrarReparacion(OrdenMantenimiento orden, LocalDate fecha, String descripcion)
            throws OrdenIncompletaException {
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria.");
        orden.cerrar(fecha, this, descripcion);
    }
}
