package modelo.personas;

import modelo.equipos.OrdenMantenimiento;
import modelo.reclamos.Reclamo;
import modelo.excepciones.ReglaNegocioException;
import modelo.excepciones.LimiteReclamosException;
import java.time.LocalDateTime;
import java.util.List;

public class Administrador {
    public static final int MAXIMO_RECLAMOS_ACTIVOS = 5;   // RN01.8

    // RN01.7 y RN01.8: un único responsable por reclamo y como máximo 5 reclamos activos por empleado.
    // Se cuentan sobre todos los reclamos registrados, así el límite no depende de quién los asignó.
    public void asignarReclamo(Reclamo reclamo, EmpleadoMantenimiento empleado, LocalDateTime fecha,
                               List<Reclamo> reclamosRegistrados) throws ReglaNegocioException {
        if (reclamo == null || empleado == null || fecha == null || reclamosRegistrados == null)
            throw new IllegalArgumentException("Reclamo, empleado y fecha son obligatorios.");
        int activos = 0;
        for (Reclamo r : reclamosRegistrados)
            if (!r.equals(reclamo) && r.estaActivo() && r.getResponsable() != null
                    && r.getResponsable().getLegajo().equals(empleado.getLegajo()))
                activos++;
        if (activos >= MAXIMO_RECLAMOS_ACTIVOS) throw new LimiteReclamosException();
        reclamo.asignarResponsable(empleado, fecha);
    }

    // RF04: el administrador asigna las órdenes de mantenimiento a los empleados.
    public void asignarOrden(OrdenMantenimiento orden, EmpleadoMantenimiento empleado) {
        if (orden == null || empleado == null) throw new IllegalArgumentException("Orden y empleado son obligatorios.");
        orden.asignarTecnico(empleado);
    }
}
