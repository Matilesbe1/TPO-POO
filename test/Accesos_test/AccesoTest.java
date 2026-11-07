package modelo.accesos;

import modelo.excepciones.AutorizacionInvalidaException;
import modelo.excepciones.IngresoDuplicadoException;
import modelo.excepciones.IngresoNoEncontradoException;
import modelo.lotes.Lote;
import modelo.personas.Guardia;
import modelo.personas.Vecino;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AccesoTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);
    private static final LocalTime NUEVE = LocalTime.of(9, 0);
    private static final LocalTime DIECIOCHO = LocalTime.of(18, 0);
    private final Vecino titular = new Vecino("30111222", "Ana Pérez");
    private final Lote lote = new Lote("10", 500, 0.05, titular);
    private final Guardia guardia = new Guardia("G1", "Pedro");
    private final Visitante visitante = new Visitante("Juan Gómez", "40111222");

    private void autorizarVisitante() throws Exception {
        titular.autorizarVisita(visitante, lote, DIA, DIA, NUEVE, DIECIOCHO, false);
    }

    @Test void ingresoValidoDentroDeLaFranja() throws Exception {
        autorizarVisitante();
        RegistroAcceso registro = guardia.registrarIngreso("40111222", lote, List.of(lote), DIA.atTime(10, 0));
        assertNotNull(registro);
        assertNull(registro.getHoraEgreso());
    }

    @Test void sinAutorizacionNoSeHabilitaElIngreso() {
        assertThrows(AutorizacionInvalidaException.class,
                () -> guardia.registrarIngreso("40111222", lote, List.of(lote), DIA.atTime(10, 0)));
    }

    @Test void fueraDeLaFranjaHorariaSeRechaza() throws Exception {
        autorizarVisitante();
        assertThrows(AutorizacionInvalidaException.class,
                () -> guardia.registrarIngreso("40111222", lote, List.of(lote), DIA.atTime(20, 0)));
        assertThrows(AutorizacionInvalidaException.class,
                () -> guardia.registrarIngreso("40111222", lote, List.of(lote), DIA.plusDays(1).atTime(10, 0)));
    }

    @Test void noPuedeVolverAIngresarSinRegistrarElEgreso() throws Exception {
        autorizarVisitante();
        guardia.registrarIngreso("40111222", lote, List.of(lote), DIA.atTime(10, 0));
        assertThrows(IngresoDuplicadoException.class,
                () -> guardia.registrarIngreso("40111222", lote, List.of(lote), DIA.atTime(11, 0)));
    }

    @Test void conElEgresoRegistradoPuedeVolverAIngresar() throws Exception {
        autorizarVisitante();
        guardia.registrarIngreso("40111222", lote, List.of(lote), DIA.atTime(10, 0));
        RegistroAcceso egreso = guardia.registrarEgreso("40111222", List.of(lote), DIA.atTime(12, 0));
        assertEquals(DIA.atTime(12, 0), egreso.getHoraEgreso());
        assertNotNull(guardia.registrarIngreso("40111222", lote, List.of(lote), DIA.atTime(15, 0)));
    }

    @Test void egresoSinIngresoSeRechaza() {
        assertThrows(IngresoNoEncontradoException.class,
                () -> guardia.registrarEgreso("40111222", List.of(lote), DIA.atTime(12, 0)));
    }

    @Test void proveedorConAutorizacionRecurrenteDe30Dias() throws Exception {
        Proveedor proveedor = new Proveedor("Jardinería SRL", "20999888", "Jardinería");
        titular.autorizarVisita(proveedor, lote, DIA, DIA.plusDays(29), NUEVE, DIECIOCHO, true);
        LocalDateTime dentroDelPeriodo = DIA.plusDays(20).atTime(10, 0);
        assertNotNull(guardia.registrarIngreso("20999888", lote, List.of(lote), dentroDelPeriodo));
    }

    @Test void autorizacionRecurrenteDeMasDe30DiasSeRechaza() {
        Proveedor proveedor = new Proveedor("Jardinería SRL", "20999888", "Jardinería");
        assertThrows(AutorizacionInvalidaException.class,
                () -> titular.autorizarVisita(proveedor, lote, DIA, DIA.plusDays(30), NUEVE, DIECIOCHO, true));
    }

    @Test void unVisitanteNoPuedeTenerAutorizacionRecurrente() {
        assertThrows(AutorizacionInvalidaException.class,
                () -> titular.autorizarVisita(visitante, lote, DIA, DIA.plusDays(5), NUEVE, DIECIOCHO, true));
    }

    @Test void soloTitularOResidentePuedenAutorizar() {
        Vecino ajeno = new Vecino("999", "Otro");
        assertThrows(AutorizacionInvalidaException.class,
                () -> ajeno.autorizarVisita(visitante, lote, DIA, DIA, NUEVE, DIECIOCHO, false));
    }

    @Test void validarVigenciaRespetaFechaYFranja() throws Exception {
        Autorizacion aut = titular.autorizarVisita(visitante, lote, DIA, DIA, NUEVE, DIECIOCHO, false);
        assertTrue(aut.validarVigencia(DIA.atTime(9, 0)));
        assertTrue(aut.validarVigencia(DIA.atTime(18, 0)));
        assertFalse(aut.validarVigencia(DIA.atTime(8, 59)));
        assertFalse(aut.validarVigencia(DIA.minusDays(1).atTime(10, 0)));
    }
}
