import controlador.ControladorExpensas;
import controlador.ControladorReclamos;
import modelo.personas.Vecino;
import modelo.lotes.Lote;
import modelo.reclamos.Reclamo;
import persistencia.Repositorio;
import persistencia.RepositorioArchivo;
import vista.VistaExpensas;
import vista.VistaPrincipal;
import vista.VistaReclamos;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Único lugar que conoce la implementación concreta del repositorio (DIP).
        Repositorio<Vecino> vecinos = new RepositorioArchivo<>("data/vecinos.dat");
        Repositorio<Lote> lotes = new RepositorioArchivo<>("data/lotes.dat");
        Repositorio<Reclamo> reclamos = new RepositorioArchivo<>("data/reclamos.dat");
        ControladorReclamos controlador = new ControladorReclamos(vecinos, lotes, reclamos);
        ControladorExpensas controladorExpensas = new ControladorExpensas(lotes);
        Scanner scanner = new Scanner(System.in);
        VistaReclamos vistaReclamos = new VistaReclamos(scanner, controlador);
        VistaExpensas vistaExpensas = new VistaExpensas(scanner, controladorExpensas);
        VistaPrincipal vista = new VistaPrincipal(scanner, vistaReclamos, vistaExpensas);
        vista.iniciar();
        scanner.close();
    }
}
