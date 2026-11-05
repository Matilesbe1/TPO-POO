package modelo;

import java.util.List;

public interface Repositorio extends Identificable {
    void guardar( Object entidad) throws Exception;
    Object buscarPorId(Object id);
    List<> listarTodos();
    void eliminar(Object id);
}