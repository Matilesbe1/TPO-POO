package modelo.equipos;

import modelo.excepciones.EquipoConFallaException;
import modelo.excepciones.OrdenIncompletaException;
import modelo.personas.Administrador;
import modelo.personas.EmpleadoMantenimiento;
import modelo.personas.Guardia;
import modelo.reclamos.Prioridad;
import modelo.reportes.ReporteEquiposFueraServicio;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EquipoTest {
    private static final LocalDate INSTALACION = LocalDate.of(2025, 1, 1);
    private static final LocalDateTime FALLA = LocalDateTime.of(2026, 11, 1, 10, 0);
    private final Guardia guardia = new Guardia("G1", "Pedro");
    private final EmpleadoMantenimiento tecnico = new EmpleadoMantenimiento("E1", "Carlos");

    private Camara camara(String codigo) { return new Camara(codigo, "Norte", INSTALACION, "1080p", 30); }
    private Luminaria luminaria(String codigo) { return new Luminaria(codigo, "Norte", INSTALACION, "LED", 50); }

    @Test void noSePuedeInstanciarEquipoSinSubclase() {
        // Equipo es abstracta: se trabaja siempre con Camara o Luminaria.
        Equipo equipo = camara("C1");
        assertEquals("Cámara", equipo.getTipo());
    }

    @Test void fallaDeCamaraGeneraOrdenDePrioridadAlta() throws Exception {
        Camara camara = camara("C1");
        OrdenMantenimiento orden = guardia.registrarFalla(camara, List.of(camara), "Sin imagen", FALLA);
        assertEquals(Prioridad.ALTA, orden.getPrioridad());
        assertEquals(EstadoEquipo.CON_FALLA, camara.getEstado());
    }

    @Test void fallaDeLuminariaGeneraOrdenDePrioridadNormal() throws Exception {
        Luminaria luminaria = luminaria("L1");
        OrdenMantenimiento orden = guardia.registrarFalla(luminaria, List.of(luminaria), "Apagada", FALLA);
        assertEquals(Prioridad.NORMAL, orden.getPrioridad());
    }

    @Test void conMasDeTresLuminariasConFallaEnElSectorLaPrioridadEsAlta() throws Exception {
        List<Equipo> equipos = new ArrayList<>();
        for (int i = 1; i <= 4; i++) equipos.add(luminaria("L" + i));
        equipos.add(camara("C1"));   // una cámara con falla no cuenta para las luminarias
        guardia.registrarFalla(equipos.get(4), equipos, "Sin imagen", FALLA);
        assertEquals(Prioridad.NORMAL, guardia.registrarFalla(equipos.get(0), equipos, "Apagada", FALLA).getPrioridad());
        assertEquals(Prioridad.NORMAL, guardia.registrarFalla(equipos.get(1), equipos, "Apagada", FALLA).getPrioridad());
        assertEquals(Prioridad.NORMAL, guardia.registrarFalla(equipos.get(2), equipos, "Apagada", FALLA).getPrioridad());
        assertEquals(Prioridad.ALTA, guardia.registrarFalla(equipos.get(3), equipos, "Apagada", FALLA).getPrioridad());
    }

    @Test void polimorfismoSinPreguntarElTipo() {
        // LSP: cualquier Equipo se usa igual, sin instanceof.
        List<Equipo> equipos = List.of(camara("C1"), luminaria("L1"));
        assertEquals(Prioridad.ALTA, equipos.get(0).calcularPrioridadFalla(1));
        assertEquals(Prioridad.NORMAL, equipos.get(1).calcularPrioridadFalla(1));
    }

    @Test void noSePuedeRegistrarOtraFallaConUnaOrdenAbierta() throws Exception {
        Camara camara = camara("C1");
        guardia.registrarFalla(camara, List.of(camara), "Sin imagen", FALLA);
        assertThrows(EquipoConFallaException.class,
                () -> guardia.registrarFalla(camara, List.of(camara), "Otra", FALLA.plusHours(1)));
    }

    @Test void cicloCompletoDeLaOrden() throws Exception {
        Camara camara = camara("C1");
        OrdenMantenimiento orden = guardia.registrarFalla(camara, List.of(camara), "Sin imagen", FALLA);
        new Administrador().asignarOrden(orden, tecnico);
        assertEquals(EstadoEquipo.EN_REPARACION, camara.getEstado());
        tecnico.registrarReparacion(orden, LocalDate.of(2026, 11, 2), "Cambio de fuente");
        assertTrue(orden.isCerrada());
        assertEquals(tecnico, orden.getTecnico());
        assertEquals(EstadoEquipo.OPERATIVO, camara.getEstado());
        assertEquals(1, camara.getHistorial().size());
    }

    @Test void noSePuedeCerrarUnaOrdenIncompleta() throws Exception {
        Camara camara = camara("C1");
        OrdenMantenimiento orden = guardia.registrarFalla(camara, List.of(camara), "Sin imagen", FALLA);
        assertThrows(OrdenIncompletaException.class, () -> orden.cerrar(null, tecnico, "Arreglada"));
        assertThrows(OrdenIncompletaException.class, () -> orden.cerrar(LocalDate.of(2026, 11, 2), null, "Arreglada"));
        assertThrows(OrdenIncompletaException.class, () -> orden.cerrar(LocalDate.of(2026, 11, 2), tecnico, " "));
        assertFalse(orden.isCerrada());
    }

    @Test void camaraSinFuncionarMasDe48HorasRequiereAviso() throws Exception {
        Camara camara = camara("C1");
        guardia.registrarFalla(camara, List.of(camara), "Sin imagen", FALLA);
        assertFalse(camara.requiereAvisoAdministrador(FALLA.plusHours(47)));
        assertTrue(camara.requiereAvisoAdministrador(FALLA.plusHours(49)));
    }

    @Test void luminariaNuncaRequiereAviso() throws Exception {
        Luminaria luminaria = luminaria("L1");
        guardia.registrarFalla(luminaria, List.of(luminaria), "Apagada", FALLA);
        assertFalse(luminaria.requiereAvisoAdministrador(FALLA.plusDays(10)));
    }

    @Test void reporteListaEquiposNoOperativosOrdenadosPorSector() throws Exception {
        Camara sur = new Camara("C2", "Sur", INSTALACION, "1080p", 30);
        Camara norte = camara("C1");
        Luminaria operativa = luminaria("L1");
        guardia.registrarFalla(sur, List.of(sur), "Sin imagen", FALLA);
        guardia.registrarFalla(norte, List.of(norte), "Sin imagen", FALLA);
        List<String> reporte = new ReporteEquiposFueraServicio(List.of(sur, operativa, norte)).generar();
        assertEquals(2, reporte.size());
        assertTrue(reporte.get(0).startsWith("Sector Norte"));
        assertTrue(reporte.get(1).startsWith("Sector Sur"));
    }
}
