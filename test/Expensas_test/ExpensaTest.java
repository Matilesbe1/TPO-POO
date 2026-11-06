package modelo.expensas;

import modelo.excepciones.AjusteInvalidoException;
import modelo.excepciones.PagoInvalidoException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExpensaTest {
    private static final YearMonth SEPTIEMBRE = YearMonth.of(2026, 9);

    // Expensa de $ 1000 que vence el 10/09/2026.
    private Expensa expensaDeMil() {
        return new Expensa(SEPTIEMBRE, 1000, 1);
    }

    @Test void importeEsPresupuestoPorCoeficiente() {
        Expensa expensa = new Expensa(SEPTIEMBRE, 100000, 0.05);
        assertEquals(5000, expensa.getImporte());
    }

    @Test void venceElDiaDiezDelMes() {
        assertEquals(LocalDate.of(2026, 9, 10), expensaDeMil().getFechaVencimiento());
    }

    @Test void presupuestoOCoeficienteInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Expensa(SEPTIEMBRE, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Expensa(SEPTIEMBRE, 1000, -1));
    }

    @Test void sinInteresHastaElVencimiento() {
        Expensa expensa = expensaDeMil();
        assertEquals(0, expensa.calcularInteres(LocalDate.of(2026, 9, 5)));
        assertEquals(0, expensa.calcularInteres(LocalDate.of(2026, 9, 10)));
    }

    @Test void interesA15DiasDelVencimiento() {
        // 1000 x 3 % / 30 x 15 días = 15
        assertEquals(15, expensaDeMil().calcularInteres(LocalDate.of(2026, 9, 25)));
    }

    @Test void interesA45DiasDelVencimiento() {
        // 1000 x 3 % / 30 x 45 días = 45
        assertEquals(45, expensaDeMil().calcularInteres(LocalDate.of(2026, 10, 25)));
    }

    @Test void pagoParcialCubrePrimeroLosIntereses() throws PagoInvalidoException {
        Expensa expensa = expensaDeMil();
        Pago pago = expensa.registrarPago(LocalDate.of(2026, 9, 25), 100, MedioPago.EFECTIVO);
        assertEquals(15, pago.getImputadoAIntereses());
        assertEquals(85, pago.getImputadoACapital());
        assertEquals(915, expensa.getCapitalPendiente());
        assertEquals(EstadoExpensa.PAGADA_PARCIALMENTE, expensa.getEstado());
    }

    @Test void pagoQueSoloAlcanzaParaIntereses() throws PagoInvalidoException {
        Expensa expensa = expensaDeMil();
        Pago pago = expensa.registrarPago(LocalDate.of(2026, 9, 25), 10, MedioPago.EFECTIVO);
        assertEquals(10, pago.getImputadoAIntereses());
        assertEquals(0, pago.getImputadoACapital());
        assertEquals(1000, expensa.getCapitalPendiente());
        assertEquals(5, expensa.calcularInteres(LocalDate.of(2026, 9, 25)));
    }

    @Test void pagoTotalDejaLaExpensaPagada() throws PagoInvalidoException {
        Expensa expensa = expensaDeMil();
        LocalDate fecha = LocalDate.of(2026, 9, 25);
        expensa.registrarPago(fecha, expensa.getDeuda(fecha), MedioPago.TRANSFERENCIA);
        assertEquals(EstadoExpensa.PAGADA, expensa.getEstado());
        assertEquals(0, expensa.getDeuda(LocalDate.of(2026, 12, 1)));
    }

    @Test void pagoMayorALaDeudaSeRechaza() {
        Expensa expensa = expensaDeMil();
        assertThrows(PagoInvalidoException.class,
                () -> expensa.registrarPago(LocalDate.of(2026, 9, 5), 1500, MedioPago.EFECTIVO));
    }

    @Test void pagoConImporteNoPositivoLanzaExcepcion() {
        Expensa expensa = expensaDeMil();
        assertThrows(IllegalArgumentException.class,
                () -> expensa.registrarPago(LocalDate.of(2026, 9, 5), -10, MedioPago.EFECTIVO));
    }

    @Test void ordenNaturalPorPeriodo() {
        Expensa octubre = new Expensa(YearMonth.of(2026, 10), 1000, 1);
        Expensa agosto = new Expensa(YearMonth.of(2026, 8), 1000, 1);
        Expensa septiembre = expensaDeMil();
        List<Expensa> expensas = new ArrayList<>(List.of(octubre, agosto, septiembre));
        Collections.sort(expensas);
        assertEquals(List.of(agosto, septiembre, octubre), expensas);
    }

    @Test void ajusteNoModificaElPagoOriginal() throws Exception {
        Expensa expensa = expensaDeMil();
        Pago pago = expensa.registrarPago(LocalDate.of(2026, 9, 5), 1000, MedioPago.TRANSFERENCIA);
        expensa.registrarAjuste(1, LocalDate.of(2026, 9, 8), "Transferencia rebotada parcialmente", -400);

        assertEquals(1000, pago.getImporte());
        assertEquals(LocalDate.of(2026, 9, 5), pago.getFecha());
        assertEquals(1, pago.getAjustes().size());
        assertEquals(600, pago.getImporteEfectivo());
        assertEquals(400, expensa.getCapitalPendiente());
        assertEquals(1, expensa.getPagos().size());
    }

    @Test void ajusteQueDejaSaldoAFavorSeRechaza() throws PagoInvalidoException {
        Expensa expensa = expensaDeMil();
        expensa.registrarPago(LocalDate.of(2026, 9, 5), 500, MedioPago.EFECTIVO);
        assertThrows(AjusteInvalidoException.class,
                () -> expensa.registrarAjuste(1, LocalDate.of(2026, 9, 6), "Error de carga", 600));
    }

    @Test void ajusteNegativoDeUnPagoQueFueAIntereses() throws Exception {
        Expensa expensa = expensaDeMil();
        LocalDate fecha = LocalDate.of(2026, 9, 25);
        // 100 = 15 de intereses + 85 de capital. Al anularlo entero vuelven 85 al capital y 15 a intereses.
        expensa.registrarPago(fecha, 100, MedioPago.EFECTIVO);
        expensa.registrarAjuste(1, fecha, "Pago anulado", -100);
        assertEquals(1000, expensa.getCapitalPendiente());
        assertEquals(15, expensa.calcularInteres(fecha));
    }

    @Test void ajustePositivoCubrePrimeroLosIntereses() throws Exception {
        Expensa expensa = expensaDeMil();
        expensa.registrarPago(LocalDate.of(2026, 9, 5), 100, MedioPago.EFECTIVO);
        LocalDate fecha = LocalDate.of(2026, 9, 25);
        // Capital 900 a los 15 días genera 13.5 de interés; el ajuste de 50 cubre eso y 36.5 de capital.
        expensa.registrarAjuste(1, fecha, "Se registró de menos", 50);
        assertEquals(0, expensa.calcularInteres(fecha));
        assertEquals(863.5, expensa.getCapitalPendiente());
    }

    @Test void ajusteDePagoInexistenteLanzaExcepcion() {
        Expensa expensa = expensaDeMil();
        assertThrows(IllegalArgumentException.class,
                () -> expensa.registrarAjuste(1, LocalDate.of(2026, 9, 6), "Error", 100));
    }

    @Test void laListaDePagosNoSePuedeModificarDesdeAfuera() throws PagoInvalidoException {
        Expensa expensa = expensaDeMil();
        Pago pago = expensa.registrarPago(LocalDate.of(2026, 9, 5), 100, MedioPago.EFECTIVO);
        expensa.getPagos().remove(pago);
        assertEquals(1, expensa.getPagos().size());
    }
}
