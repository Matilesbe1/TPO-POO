package persistencia;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RepositorioMemoriaTest {
    static class Elemento implements Identificable {
        private final String id;
        private String valor;
        Elemento(String id, String valor) { this.id = id; this.valor = valor; }
        public String getId() { return id; }
    }
    @Test void guardarBuscarListarYActualizar() {
        Repositorio<Elemento> repo = new RepositorioMemoria<>();
        Elemento uno = new Elemento("1", "A");
        repo.guardar(uno);
        assertTrue(repo.buscarPorId("1").isPresent());
        assertEquals(1, repo.listar().size());
        Elemento actualizado = new Elemento("1", "B");
        repo.actualizar(actualizado);
        assertSame(actualizado, repo.buscarPorId("1").orElseThrow());
    }
}
