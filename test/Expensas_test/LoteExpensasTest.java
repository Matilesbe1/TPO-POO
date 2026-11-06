package modelo.expensas;

import modelo.excepciones.ExpensaDuplicadaException;
import modelo.excepciones.PagoInvalidoException;
import modelo.lotes.Lote;
import modelo.personas.Vecino;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.YearMonth;
import static org.junit.jupiter.api.Assertions.*;

class LoteExpensasTest {
    private final Vecino titular = new Vecino("30111222", "Ana Pérez");

    private Lote loteConCoeficienteUno() {
        return new Lote("10", 500, 1, titular);
    }

    // Emite expensas de $ 1000 para los meses indicados de 2026.
    private Lote loteConExpensas(int... meses) throws ExpensaDuplicadaException {
        Lote lote = loteConCoeficienteUno();
        for (int mes : meses) lote.emitirExpensa(YearMonth.of(2026, mes), 1000);
        return lote;
    }

    @Test void emiteConElCoeficienteDelLote() throws ExpensaDuplicadaException {
        Lote lote = new Lote("11", 300, 0.04, titular);
        Expensa expensa = lote.emitirExpensa(YearMonth.of(2026, 9), 250000);
        assertEquals(10000, expensa.getImporte());
        assertEquals(1, lote.getExpensas().size());
    }

    @Test void noSePuedeEmitirDosVecesElMismoPeriodo() throws ExpensaDuplicadaException {
        Lote lote = loteConExpensas(9);
        assertThrows(ExpensaDuplicadaException.class, () -> lote.emitirExpensa(YearMonth.of(2026, 9), 1000));
    }

    @Test void pagoSaldaPrimeroElPeriodoMasAntiguo() throws Exception {
        // Se emite primero septiembre y después agosto, para comprobar que se ordenan por período.
        Lote lote = loteConExpensas(9, 8);
        lote.imputarPago(LocalDate.of(2026, 8, 5), 1500, MedioPago.EFECTIVO);
        assertTrue(lote.buscarExpensa(YearMonth.of(2026, 8)).estaPagada());
        assertEquals(500, lote.buscarExpensa(YearMonth.of(2026, 9)).getCapitalPendiente());
    }

    @Test void pagoQueSuperaLaDeudaTotalSeRechaza() throws ExpensaDuplicadaException {
        Lote lote = loteConExpensas(8, 9);
        assertThrows(PagoInvalidoException.class,
                () -> lote.imputarPago(LocalDate.of(2026, 8, 5), 2500, MedioPago.EFECTIVO));
    }

    @Test void pagoALoteSinDeudaSeRechaza() {
        Lote lote = loteConCoeficienteUno();
        assertThrows(PagoInvalidoException.class,
                () -> lote.imputarPago(LocalDate.of(2026, 8, 5), 100, MedioPago.EFECTIVO));
    }

    @Test void conTresPeriodosImpagosNoEsMoroso() throws ExpensaDuplicadaException {
        Lote lote = loteConExpensas(6, 7, 8);
        LocalDate fecha = LocalDate.of(2026, 9, 1);
        lote.actualizarMorosidad(fecha);
        assertEquals(3, lote.contarPeriodosImpagos(fecha));
        assertFalse(lote.esMoroso(fecha));
        assertTrue(lote.getNotificaciones().isEmpty());
    }

    @Test void conCuatroPeriodosImpagosEsMorosoYSeNotificaUnaVez() throws ExpensaDuplicadaException {
        Lote lote = loteConExpensas(6, 7, 8, 9);
        LocalDate fecha = LocalDate.of(2026, 9, 15);
        lote.actualizarMorosidad(fecha);
        lote.actualizarMorosidad(fecha.plusDays(1));
        assertTrue(lote.esMoroso(fecha));
        assertEquals(1, lote.getNotificaciones().size());
        assertEquals(titular, lote.getNotificaciones().get(0).getDestinatario());
    }

    @Test void alRegularizarLaDeudaDejaDeSerMoroso() throws Exception {
        Lote lote = loteConExpensas(6, 7, 8, 9);
        LocalDate fecha = LocalDate.of(2026, 9, 15);
        lote.actualizarMorosidad(fecha);
        lote.imputarPago(fecha, lote.getDeudaTotal(fecha), MedioPago.TRANSFERENCIA);
        assertFalse(lote.esMoroso(fecha));
        assertEquals(0, lote.getDeudaTotal(fecha));
        assertEquals(1, lote.getNotificaciones().size());
    }

    @Test void volverASerMorosoGeneraOtraNotificacion() throws Exception {
        Lote lote = loteConExpensas(6, 7, 8, 9);
        LocalDate fecha = LocalDate.of(2026, 9, 15);
        lote.actualizarMorosidad(fecha);
        lote.imputarPago(fecha, lote.getDeudaTotal(fecha), MedioPago.TRANSFERENCIA);
        for (int mes = 10; mes <= 12; mes++) lote.emitirExpensa(YearMonth.of(2026, mes), 1000);
        lote.emitirExpensa(YearMonth.of(2027, 1), 1000);
        lote.actualizarMorosidad(LocalDate.of(2027, 1, 15));
        assertEquals(2, lote.getNotificaciones().size());
    }
}
