package persistencia;

import modelo.lotes.Lote;
import modelo.personas.Vecino;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.YearMonth;
import static org.junit.jupiter.api.Assertions.*;

class RepositorioArchivoTest {
    private final File archivo = new File(System.getProperty("java.io.tmpdir"), "aromos-test-lotes.dat");

    @AfterEach void borrarArchivo() {
        archivo.delete();
    }

    @Test void siElArchivoNoExisteArrancaVacio() {
        archivo.delete();
        Repositorio<Lote> repo = new RepositorioArchivo<>(archivo.getPath());
        assertTrue(repo.listar().isEmpty());
    }

    @Test void loGuardadoSeRecuperaDesdeOtroRepositorio() throws Exception {
        Lote lote = new Lote("10", 500, 0.05, new Vecino("30111222", "Ana Pérez"));
        lote.emitirExpensa(YearMonth.of(2026, 9), 100000);
        Repositorio<Lote> repo = new RepositorioArchivo<>(archivo.getPath());
        repo.guardar(lote);

        Repositorio<Lote> otroRepo = new RepositorioArchivo<>(archivo.getPath());
        Lote recuperado = otroRepo.buscarPorId("10").orElseThrow();

        assertEquals(lote, recuperado);
        assertEquals("Ana Pérez", recuperado.getTitular().getNombre());
        assertEquals(1, recuperado.getExpensas().size());
        assertEquals(5000, recuperado.getExpensas().get(0).getImporte());
    }

    @Test void actualizarPersisteLosCambios() throws Exception {
        Lote lote = new Lote("10", 500, 1, new Vecino("30111222", "Ana Pérez"));
        Repositorio<Lote> repo = new RepositorioArchivo<>(archivo.getPath());
        repo.guardar(lote);
        lote.emitirExpensa(YearMonth.of(2026, 9), 1000);
        repo.actualizar(lote);

        Lote recuperado = new RepositorioArchivo<Lote>(archivo.getPath()).buscarPorId("10").orElseThrow();
        assertEquals(1, recuperado.getExpensas().size());
    }
}
