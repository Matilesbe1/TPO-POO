package persistencia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RepositorioMemoria<T extends Identificable> implements Repositorio<T> {
    private final Map<String, T> datos = new HashMap<>();

    @Override
    public void guardar(T objeto) {
        if (objeto == null) throw new IllegalArgumentException("El objeto no puede ser null.");
        datos.put(objeto.getId(), objeto);
    }

    @Override
    public Optional<T> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<T> listar() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public void actualizar(T objeto) {
        if (objeto == null) throw new IllegalArgumentException("El objeto no puede ser null.");
        if (!datos.containsKey(objeto.getId())) {
            throw new IllegalArgumentException("No existe un objeto con ese ID.");
        }
        datos.put(objeto.getId(), objeto);
    }
}
