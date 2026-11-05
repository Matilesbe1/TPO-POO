package ar.aromos.modelo.excepciones;
public class PlazoVencidoException extends ReglaNegocioException {
    public PlazoVencidoException() { super("Se venció el plazo para reabrir el reclamo."); }
    public PlazoVencidoException(String mensaje) { super(mensaje); }
}
