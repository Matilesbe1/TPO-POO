package modelo.reportes;

import modelo.equipos.Equipo;
import modelo.equipos.EstadoEquipo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReporteEquiposFueraServicio implements Reporte {
    private final List<Equipo> equipos;

    public ReporteEquiposFueraServicio(List<Equipo> equipos) {
        if (equipos == null) throw new IllegalArgumentException("La lista de equipos es obligatoria.");
        this.equipos = new ArrayList<>(equipos);
    }

    @Override public List<String> generar() {
        List<Equipo> fueraDeServicio = new ArrayList<>();
        for (Equipo equipo : equipos)
            if (equipo.getEstado() != EstadoEquipo.OPERATIVO) fueraDeServicio.add(equipo);
        // Orden alternativo con Comparator: por sector y después por código.
        fueraDeServicio.sort(Comparator.comparing(Equipo::getSector).thenComparing(Equipo::getCodigo));
        List<String> resultado = new ArrayList<>();
        for (Equipo equipo : fueraDeServicio)
            resultado.add("Sector " + equipo.getSector() + " | " + equipo.getTipo() + " " + equipo.getCodigo()
                    + " | " + equipo.getEstado() + " | Falla desde: " + equipo.getFechaHoraFalla());
        return resultado;
    }
}
