package modelo.excepciones;
public class PagoInvalidoException extends ReglaNegocioException {
    public PagoInvalidoException() { super("El pago no se puede registrar."); }
    public PagoInvalidoException(String mensaje) { super(mensaje); }
}
