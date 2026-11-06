package modelo.excepciones;
public class TransicionInvalidaException extends ReglaNegocioException {
    public TransicionInvalidaException() { super("El reclamo solo puede avanzar al estado siguiente."); }
    public TransicionInvalidaException(String mensaje) { super(mensaje); }
}
