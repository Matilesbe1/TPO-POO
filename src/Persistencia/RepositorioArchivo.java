package persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Fabricación pura: es la única clase que trabaja con archivos.
// El resto del sistema la usa a través de la interfaz Repositorio (DIP).
public class RepositorioArchivo<T extends Identificable> implements Repositorio<T> {
    private final File archivo;
    private final Map<String, T> datos;

    public RepositorioArchivo(String ruta) {
        if (ruta == null || ruta.isBlank()) throw new IllegalArgumentException("La ruta del archivo es obligatoria.");
        this.archivo = new File(ruta);
        this.datos = cargar();
    }

    @Override
    public void guardar(T objeto) {
        if (objeto == null) throw new IllegalArgumentException("El objeto no puede ser null.");
        datos.put(objeto.getId(), objeto);
        escribir();
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
        if (!datos.containsKey(objeto.getId())) throw new IllegalArgumentException("No existe un objeto con ese ID.");
        datos.put(objeto.getId(), objeto);
        escribir();
    }

    // La primera vez el archivo no existe: el repositorio arranca vacío.
    private Map<String, T> cargar() {
        if (!archivo.exists()) return new HashMap<>();
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            return (Map<String, T>) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo leer el archivo " + archivo.getPath() + ": " + e.getMessage(), e);
        }
    }

    private void escribir() {
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) carpeta.mkdirs();
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(archivo))) {
            salida.writeObject(new HashMap<>(datos));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo " + archivo.getPath() + ": " + e.getMessage(), e);
        }
    }
}
