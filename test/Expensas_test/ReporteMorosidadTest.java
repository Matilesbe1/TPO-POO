package modelo.reportes;

import modelo.lotes.Lote;
import modelo.personas.Vecino;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReporteMorosidadTest {
    private Lote loteConExpensas(String numero, double presupuesto, int cantidadMeses) throws Exception {
        Lote lote = new Lote(numero, 500, 1, new Vecino("doc-" + numero, "Titular " + numero));
        for (int mes = 1; mes <= cantidadMeses; mes++) lote.emitirExpensa(YearMonth.of(2026, mes), presupuesto);
        return lote;
    }

    @Test void listaSoloMorososOrdenadosPorDeudaDescendente() throws Exception {
        Lote morosoChico = loteConExpensas("1", 1000, 4);
        Lote morosoGrande = loteConExpensas("2", 5000, 4);
        Lote alDia = loteConExpensas("3", 1000, 3);
        LocalDate fecha = LocalDate.of(2026, 4, 15);

        List<String> reporte = new ReporteMorosidad(List.of(morosoChico, alDia, morosoGrande), fecha).generar();

        assertEquals(2, reporte.size());
        assertTrue(reporte.get(0).startsWith("Lote 2"));
        assertTrue(reporte.get(1).startsWith("Lote 1"));
    }

    @Test void sinMorososElReporteEstaVacio() throws Exception {
        Lote alDia = loteConExpensas("3", 1000, 2);
        assertTrue(new ReporteMorosidad(List.of(alDia), LocalDate.of(2026, 4, 15)).generar().isEmpty());
    }
}
