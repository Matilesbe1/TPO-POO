package ar.aromos.modelo.excepciones;
public class LimiteReclamosException extends ReglaNegocioException {
    public LimiteReclamosException() { super("El empleado ya tiene cinco reclamos en curso."); }
    public LimiteReclamosException(String mensaje) { super(mensaje); }
}
