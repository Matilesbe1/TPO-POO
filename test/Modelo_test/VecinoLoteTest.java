package modelo;

import modelo.personas.Vecino;
import modelo.lotes.Lote;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class VecinoLoteTest {
    @Test void vecinosConMismoDocumentoSonIguales() {
        Vecino a = new Vecino("123", "Ana");
        Vecino b = new Vecino("123", "Ana distinta");
        assertEquals(a, b);
        HashSet<Vecino> conjunto = new HashSet<>();
        conjunto.add(a); conjunto.add(b);
        assertEquals(1, conjunto.size());
    }
    @Test void loteSeIdentificaPorNumero() {
        Vecino v = new Vecino("123", "Ana");
        Lote a = new Lote("10", 200, 1.2, v);
        Lote b = new Lote("10", 300, 1.5, v);
        assertEquals(a, b);
    }
}
