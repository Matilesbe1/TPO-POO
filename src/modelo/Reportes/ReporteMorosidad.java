package modelo.reportes;

import modelo.lotes.Lote;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReporteMorosidad implements Reporte {
    private final List<Lote> lotes;
    private final LocalDate fecha;

    public ReporteMorosidad(List<Lote> lotes, LocalDate fecha) {
        if (lotes == null || fecha == null) throw new IllegalArgumentException("Lotes y fecha son obligatorios.");
        this.lotes = new ArrayList<>(lotes);
        this.fecha = fecha;
    }

    @Override public List<String> generar() {
        List<Lote> morosos = new ArrayList<>();
        for (Lote lote : lotes)
            if (lote.esMoroso(fecha)) morosos.add(lote);
        // Orden alternativo con Comparator: primero los de mayor deuda.
        morosos.sort(Comparator.comparingDouble((Lote lote) -> lote.getDeudaTotal(fecha)).reversed());
        List<String> resultado = new ArrayList<>();
        for (Lote lote : morosos)
            resultado.add("Lote " + lote.getNumero()
                    + " | Titular: " + lote.getTitular().getNombre() + " (" + lote.getTitular().getDocumento() + ")"
                    + " | Períodos impagos: " + lote.contarPeriodosImpagos(fecha)
                    + " | Deuda: $ " + lote.getDeudaTotal(fecha));
        return resultado;
    }
}
