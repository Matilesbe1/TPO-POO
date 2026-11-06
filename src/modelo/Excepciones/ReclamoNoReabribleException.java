package modelo.excepciones;
public class ReclamoNoReabribleException extends ReglaNegocioException {
    public ReclamoNoReabribleException() { super("El reclamo no está resuelto, no se puede reabrir."); }
    public ReclamoNoReabribleException(String mensaje) { super(mensaje); }
}
