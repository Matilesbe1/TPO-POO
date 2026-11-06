package modelo.excepciones;
public class ExpensaDuplicadaException extends ReglaNegocioException {
    public ExpensaDuplicadaException() { super("Ya existe una expensa para ese período."); }
    public ExpensaDuplicadaException(String mensaje) { super(mensaje); }
}
