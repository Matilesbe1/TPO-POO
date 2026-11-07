package modelo.excepciones;
public class IngresoDuplicadoException extends ReglaNegocioException {
    public IngresoDuplicadoException(String mensaje) { super(mensaje); }
}
