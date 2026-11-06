package modelo.personas;

import modelo.lotes.Lote;
import modelo.reclamos.*;
import modelo.excepciones.ReglaNegocioException;
import persistencia.Identificable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Vecino implements Identificable, Serializable {
    private final String documento;
    private String nombre;

    public Vecino(String documento, String nombre) {
        if (documento == null || documento.isBlank() || nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Documento y nombre son obligatorios.");
        this.documento = documento;
        this.nombre = nombre;
    }
    @Override public String getId() { return documento; }
    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public void cambiarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre inválido.");
        this.nombre = nombre;
    }

    public Reclamo presentarReclamo(Lote lote, TipoReclamo tipo, String descripcion, LocalDateTime fecha)
            throws ReglaNegocioException {
        if (lote == null || !lote.estaAutorizado(this))
            throw new modelo.excepciones.ReclamoNoAutorizadoException();
        Reclamo reclamo = new Reclamo(lote, this, tipo, descripcion, fecha);
        lote.agregarReclamo(reclamo);
        return reclamo;
    }

    @Override public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof Vecino)) return false;
        Vecino vecino = (Vecino) otro;
        return documento.equals(vecino.documento);
    }
    @Override public int hashCode() { return Objects.hash(documento); }
}
