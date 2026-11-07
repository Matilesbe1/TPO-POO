package modelo.reclamos;

import modelo.excepciones.LimiteReclamosException;
import modelo.excepciones.PlazoVencidoException;
import modelo.excepciones.ReclamoNoAutorizadoException;
import modelo.excepciones.ReclamoNoReabribleException;
import modelo.excepciones.TransicionInvalidaException;
import modelo.lotes.Lote;
import modelo.personas.Administrador;
import modelo.personas.EmpleadoMantenimiento;
import modelo.personas.Vecino;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReclamoTest {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 1, 10, 0);
    private final Vecino titular = new Vecino("30111222", "Ana Pérez");
    private final Lote lote = new Lote("10", 500, 0.05, titular);
    private final EmpleadoMantenimiento empleado = new EmpleadoMantenimiento("E1", "Carlos");

    private Reclamo reclamo(TipoReclamo tipo) throws Exception {
        return titular.presentarReclamo(lote, tipo, "Descripción", INICIO);
    }

    // Lleva el reclamo hasta RESUELTO en la fecha indicada.
    private Reclamo reclamoResuelto(LocalDateTime fechaResolucion) throws Exception {
        Reclamo r = reclamo(TipoReclamo.MANTENIMIENTO);
        r.asignarResponsable(empleado, INICIO);
        r.cambiarEstado(EstadoReclamo.EN_CURSO, "Trabajando", INICIO);
        r.cambiarEstado(EstadoReclamo.RESUELTO, "Listo", fechaResolucion);
        return r;
    }

    @Test void laPrioridadDependeDelTipo() throws Exception {
        assertEquals(Prioridad.ALTA, reclamo(TipoReclamo.SEGURIDAD).getPrioridad());
        assertEquals(Prioridad.MEDIA, reclamo(TipoReclamo.MANTENIMIENTO).getPrioridad());
        assertEquals(Prioridad.BAJA, reclamo(TipoReclamo.CONVIVENCIA).getPrioridad());
        assertEquals(Prioridad.BAJA, reclamo(TipoReclamo.ADMINISTRATIVO).getPrioridad());
    }

    @Test void soloTitularOResidentePuedenPresentar() throws Exception {
        Vecino ajeno = new Vecino("999", "Otro");
        assertThrows(ReclamoNoAutorizadoException.class,
                () -> ajeno.presentarReclamo(lote, TipoReclamo.CONVIVENCIA, "Ruidos", INICIO));
        lote.agregarResidente(ajeno);
        assertNotNull(ajeno.presentarReclamo(lote, TipoReclamo.CONVIVENCIA, "Ruidos", INICIO));
    }

    @Test void arrancaAbiertoConHistorial() throws Exception {
        Reclamo r = reclamo(TipoReclamo.CONVIVENCIA);
        assertEquals(EstadoReclamo.ABIERTO, r.getEstado());
        assertEquals(1, r.getHistorial().size());
    }

    @Test void asignarLoPasaAAsignadoYRegistraHistorial() throws Exception {
        Reclamo r = reclamo(TipoReclamo.CONVIVENCIA);
        r.asignarResponsable(empleado, INICIO);
        assertEquals(EstadoReclamo.ASIGNADO, r.getEstado());
        assertEquals(empleado, r.getResponsable());
        assertEquals(2, r.getHistorial().size());
    }

    @Test void soloPuedeAvanzarAlEstadoSiguiente() throws Exception {
        Reclamo r = reclamo(TipoReclamo.CONVIVENCIA);
        assertThrows(TransicionInvalidaException.class,
                () -> r.cambiarEstado(EstadoReclamo.EN_CURSO, "Salteo", INICIO));
        assertThrows(TransicionInvalidaException.class,
                () -> r.cambiarEstado(EstadoReclamo.ASIGNADO, "Sin responsable", INICIO));
    }

    @Test void unReclamoTieneUnUnicoResponsable() throws Exception {
        Reclamo r = reclamo(TipoReclamo.CONVIVENCIA);
        r.asignarResponsable(empleado, INICIO);
        assertThrows(TransicionInvalidaException.class,
                () -> r.asignarResponsable(new EmpleadoMantenimiento("E2", "Otra"), INICIO));
    }

    @Test void unEmpleadoNoPuedeTenerMasDeCincoReclamosActivos() throws Exception {
        Administrador administrador = new Administrador();
        List<Reclamo> registrados = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Reclamo r = titular.presentarReclamo(lote, TipoReclamo.CONVIVENCIA, "Reclamo " + i, INICIO.plusMinutes(i));
            registrados.add(r);
            administrador.asignarReclamo(r, empleado, INICIO, registrados);
        }
        Reclamo sexto = titular.presentarReclamo(lote, TipoReclamo.CONVIVENCIA, "Sexto", INICIO.plusMinutes(10));
        registrados.add(sexto);
        assertThrows(LimiteReclamosException.class,
                () -> administrador.asignarReclamo(sexto, empleado, INICIO, registrados));
    }

    @Test void reabrirDentroDeLosCincoDias() throws Exception {
        Reclamo r = reclamoResuelto(INICIO);
        r.reabrir(INICIO.plusDays(4));
        assertEquals(EstadoReclamo.ABIERTO, r.getEstado());
        assertEquals("Reabierto por el vecino", r.getHistorial().get(r.getHistorial().size() - 1).getObservacion());
    }

    @Test void reabrirDespuesDeCincoDiasLoCierra() throws Exception {
        Reclamo r = reclamoResuelto(INICIO);
        assertThrows(PlazoVencidoException.class, () -> r.reabrir(INICIO.plusDays(6)));
        assertEquals(EstadoReclamo.CERRADO, r.getEstado());
    }

    @Test void noSePuedeReabrirSiNoEstaResuelto() throws Exception {
        Reclamo r = reclamo(TipoReclamo.CONVIVENCIA);
        assertThrows(ReclamoNoReabribleException.class, () -> r.reabrir(INICIO.plusDays(1)));
    }

    @Test void cierreAutomaticoALosCincoDiasDeResuelto() throws Exception {
        Reclamo r = reclamoResuelto(INICIO);
        r.cerrarSiVencido(INICIO.plusDays(5));
        assertEquals(EstadoReclamo.RESUELTO, r.getEstado());
        r.cerrarSiVencido(INICIO.plusDays(6));
        assertEquals(EstadoReclamo.CERRADO, r.getEstado());
    }

    @Test void prioridadAltaSinAtenderSeEscalaALas25Horas() throws Exception {
        Reclamo r = reclamo(TipoReclamo.SEGURIDAD);
        assertFalse(r.escalar(INICIO.plusHours(23)));
        assertTrue(r.escalar(INICIO.plusHours(25)));
        assertTrue(r.isEscalado());
        assertFalse(r.escalar(INICIO.plusHours(30)));
    }

    @Test void prioridadBajaNoSeEscala() throws Exception {
        Reclamo r = reclamo(TipoReclamo.CONVIVENCIA);
        assertFalse(r.escalar(INICIO.plusDays(3)));
    }
}
