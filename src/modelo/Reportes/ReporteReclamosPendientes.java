package modelo.reportes;

import ar.aromos.modelo.reclamos.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReporteReclamosPendientes implements Reporte {
    private final List<Reclamo> reclamos;
    public ReporteReclamosPendientes(List<Reclamo> reclamos) {
        this.reclamos = new ArrayList<>(reclamos);
    }
    @Override public List<String> generar() {
        List<Reclamo> pendientes = new ArrayList<>();
        for (Reclamo reclamo : reclamos)
            if (reclamo.getEstado() != EstadoReclamo.CERRADO) pendientes.add(reclamo);
        pendientes.sort(Reclamo.comparadorPorPrioridadYFecha());
        List<String> resultado = new ArrayList<>();
        for (Reclamo r : pendientes)
            resultado.add(r.getId() + " | " + r.getPrioridad() + " | " + r.getEstado() + " | " + r.getDescripcion());
        return resultado;
    }
}
