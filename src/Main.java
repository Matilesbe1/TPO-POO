

import controlador.ControladorReclamos;
import modelo.personas.Vecino;
import modelo.lotes.Lote;
import modelo.reclamos.Reclamo;
import persistencia.Repositorio;
import persistencia.RepositorioMemoria;
import vista.VistaPrincipal;
import vista.VistaReclamos;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Repositorio<Vecino> vecinos = new RepositorioMemoria<>();
        Repositorio<Lote> lotes = new RepositorioMemoria<>();
        Repositorio<Reclamo> reclamos = new RepositorioMemoria<>();
        ControladorReclamos controlador = new ControladorReclamos(vecinos, lotes, reclamos);
        Scanner scanner = new Scanner(System.in);
        VistaReclamos vistaReclamos = new VistaReclamos(scanner, controlador);
        VistaPrincipal vista = new VistaPrincipal(scanner, vistaReclamos);
        vista.iniciar();
        scanner.close();
    }
}
