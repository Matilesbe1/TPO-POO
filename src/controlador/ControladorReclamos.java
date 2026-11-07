package controlador;

import modelo.personas.*;
import modelo.lotes.Lote;
import modelo.reclamos.*;
import modelo.excepciones.ReglaNegocioException;
import modelo.reportes.ReporteReclamosPendientes;
import persistencia.Repositorio;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public void agregarResidente(String numeroLote, String documento) {
        Lote lote = lotes.buscarPorId(numeroLote)
                .orElseThrow(() -> new IllegalArgumentException("No existe el lote."));
        Vecino residente = vecinos.buscarPorId(documento)
                .orElseThrow(() -> new IllegalArgumentException("No existe el vecino."));
        lote.agregarResidente(residente);
        lotes.actualizar(lote);
    }
    public void asignar(String idReclamo, EmpleadoMantenimiento empleado, LocalDateTime fecha,
                        Administrador administrador) throws ReglaNegocioException {
        Reclamo r = obtenerReclamo(idReclamo);
        administrador.asignarReclamo(r, empleado, fecha, reclamos.listar());
        reclamos.actualizar(r);
    }
    public void comentar(String idReclamo, String autor, String texto, LocalDateTime fecha) {
        Reclamo r = obtenerReclamo(idReclamo);
        r.agregarComentario(autor, texto, fecha);
        reclamos.actualizar(r);
    }
    // RN01.6 y RN01.9: escala los de prioridad alta sin atender y cierra los resueltos hace más de 5 días.
    public List<String> revisarPlazos(LocalDateTime fecha) {
        List<String> resultado = new ArrayList<>();
        for (Reclamo r : reclamos.listar()) {
            EstadoReclamo antes = r.getEstado();
            if (r.escalar(fecha)) resultado.add("Escalado al administrador: " + r.getId() + " | " + r.getDescripcion());
            r.cerrarSiVencido(fecha);
            if (antes != r.getEstado()) resultado.add("Cerrado por plazo vencido: " + r.getId());
            reclamos.actualizar(r);
        }
        return resultado;
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
