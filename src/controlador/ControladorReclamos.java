package ar.aromos.controlador;

import modelo.personas.*;
import modelo.lotes.Lote;
import modelo.reclamos.*;
import modelo.excepciones.ReglaNegocioException;
import modelo.reportes.ReporteReclamosPendientes;
import persistencia.Repositorio;
import java.time.LocalDateTime;
import java.util.List;

public class ControladorReclamos {
    private final Repositorio<Vecino> vecinos;
    private final Repositorio<Lote> lotes;
    private final Repositorio<Reclamo> reclamos;

    public ControladorReclamos(Repositorio<Vecino> vecinos, Repositorio<Lote> lotes, Repositorio<Reclamo> reclamos) {
        this.vecinos = vecinos; this.lotes = lotes; this.reclamos = reclamos;
    }
    public void altaVecino(String documento, String nombre) {
        vecinos.guardar(new Vecino(documento, nombre));
    }
    public void altaLote(String numero, double superficie, double coeficiente, String documentoTitular) {
        Vecino titular = vecinos.buscarPorId(documentoTitular)
                .orElseThrow(() -> new IllegalArgumentException("No existe el vecino titular."));
        lotes.guardar(new Lote(numero, superficie, coeficiente, titular));
    }
    public String presentarReclamo(String documento, String numeroLote, TipoReclamo tipo, String descripcion, LocalDateTime fecha) throws ReglaNegocioException {
        Vecino vecino = vecinos.buscarPorId(documento)
                .orElseThrow(() -> new IllegalArgumentException("No existe el vecino."));
        Lote lote = lotes.buscarPorId(numeroLote)
                .orElseThrow(() -> new IllegalArgumentException("No existe el lote."));
        Reclamo reclamo = vecino.presentarReclamo(lote, tipo, descripcion, fecha);
        reclamos.guardar(reclamo);
        lotes.actualizar(lote);
        return reclamo.getId();
    }
    public void asignar(String idReclamo, EmpleadoMantenimiento empleado, LocalDateTime fecha,
                        Administrador administrador) throws ReglaNegocioException {
        Reclamo r = obtenerReclamo(idReclamo);
        administrador.asignarReclamo(r, empleado, fecha);
        reclamos.actualizar(r);
    }
    public void avanzar(String idReclamo, EstadoReclamo nuevo, String observacion, LocalDateTime fecha)
            throws ReglaNegocioException {
        Reclamo r = obtenerReclamo(idReclamo);
        r.cambiarEstado(nuevo, observacion, fecha);
        reclamos.actualizar(r);
    }
    public void reabrir(String idReclamo, LocalDateTime fecha) throws ReglaNegocioException {
        Reclamo r = obtenerReclamo(idReclamo);
        r.reabrir(fecha);
        reclamos.actualizar(r);
    }
    public List<String> reportePendientes() {
        return new ReporteReclamosPendientes(reclamos.listar()).generar();
    }
    private Reclamo obtenerReclamo(String id) {
        return reclamos.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("No existe el reclamo."));
    }
}
