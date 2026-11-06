package persistencia;

import java.util.List;
import java.util.Optional;

public interface Repositorio<T extends Identificable> {
    void guardar(T objeto);
    Optional<T> buscarPorId(String id);
    List<T> listar();
    void actualizar(T objeto);
}
