package ar.aromos.modelo.excepciones;
public class ReclamoNoAutorizadoException extends ReglaNegocioException {
    public ReclamoNoAutorizadoException() { super("El vecino no es titular ni residente autorizado para ese lote."); }
    public ReclamoNoAutorizadoException(String mensaje) { super(mensaje); }
}
