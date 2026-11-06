package modelo.excepciones;
public class AjusteInvalidoException extends ReglaNegocioException {
    public AjusteInvalidoException() { super("El ajuste no se puede registrar."); }
    public AjusteInvalidoException(String mensaje) { super(mensaje); }
}
