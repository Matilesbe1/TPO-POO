package modelo.equipos;

import java.io.Serializable;
import java.time.LocalDate;
import modelo.reclamos.Prioridad;
import modelo.personas.EmpleadoMantenimiento;
import modelo.excepciones.OrdenIncompletaException;

public class OrdenMantenimiento implements Serializable {
    private final String numero;
    private final Equipo equipo;
    private final Prioridad prioridad;
    private final String descripcionTrabajo;
    private final LocalDate fechaApertura;
    private LocalDate fechaCierre;
    private EmpleadoMantenimiento tecnico;
    private String descripcionReparacion;
    private boolean cerrada;

    // Solo Equipo crea sus órdenes (Creador), por eso el constructor no es público.
    OrdenMantenimiento(Equipo equipo, int secuencia, Prioridad prioridad,
                       String descripcionTrabajo, LocalDate fechaApertura) {
        if (equipo == null || prioridad == null || descripcionTrabajo == null
                || descripcionTrabajo.isBlank() || fechaApertura == null)
            throw new IllegalArgumentException("Datos de apertura de orden incompletos.");
        this.numero = equipo.getCodigo() + "-" + secuencia;
        this.equipo = equipo;
        this.prioridad = prioridad;
        this.descripcionTrabajo = descripcionTrabajo;
        this.fechaApertura = fechaApertura;
    }

    public void asignarTecnico(EmpleadoMantenimiento tecnico) {
        if (tecnico == null) throw new IllegalArgumentException("El técnico es obligatorio.");
        if (cerrada) throw new IllegalArgumentException("La orden ya está cerrada.");
        this.tecnico = tecnico;
        equipo.marcarEnReparacion();
    }

    // RN03.6: no se puede cerrar sin fecha de cierre, técnico responsable y descripción de la reparación.
    public void cerrar(LocalDate fecha, EmpleadoMantenimiento tecnico, String descripcion)
            throws OrdenIncompletaException {
        if (cerrada) throw new OrdenIncompletaException("La orden ya está cerrada.");
        if (fecha == null || tecnico == null || descripcion == null || descripcion.isBlank())
            throw new OrdenIncompletaException("Para cerrar: fecha, técnico y descripción son obligatorios.");
        if (fecha.isBefore(fechaApertura))
            throw new OrdenIncompletaException("La fecha de cierre no puede ser anterior a la apertura.");
        this.fechaCierre = fecha;
        this.tecnico = tecnico;
        this.descripcionReparacion = descripcion;
        this.cerrada = true;
        equipo.marcarOperativo();
    }

    public String getNumero() { return numero; }
    public Equipo getEquipo() { return equipo; }
    public Prioridad getPrioridad() { return prioridad; }
    public String getDescripcionTrabajo() { return descripcionTrabajo; }
    public LocalDate getFechaApertura() { return fechaApertura; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public EmpleadoMantenimiento getTecnico() { return tecnico; }
    public String getDescripcionReparacion() { return descripcionReparacion; }
    public boolean isCerrada() { return cerrada; }
}
