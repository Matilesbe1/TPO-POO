package ar.aromos.modelo.personas;

import modelo.reclamos.Reclamo;
import modelo.excepciones.ReglaNegocioException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Administrador {
    private final List<Reclamo> reclamosAsignados = new ArrayList<>();

    public void asignarReclamo(Reclamo reclamo, EmpleadoMantenimiento empleado, LocalDateTime fecha)
            throws ReglaNegocioException {
        if (reclamo == null || empleado == null || fecha == null)
            throw new IllegalArgumentException("Reclamo, empleado y fecha son obligatorios.");
        long enCurso = reclamosAsignados.stream()
                .filter(r -> r.getResponsable() != null
                        && r.getResponsable().getLegajo().equals(empleado.getLegajo())
                        && r.getEstado() == ar.aromos.modelo.reclamos.EstadoReclamo.EN_CURSO)
                .count();
        if (enCurso >= 5) throw new ar.aromos.modelo.excepciones.LimiteReclamosException();
        reclamo.asignarResponsable(empleado, fecha);
        if (!reclamosAsignados.contains(reclamo)) reclamosAsignados.add(reclamo);
    }
}
