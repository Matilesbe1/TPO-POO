import controlador.ControladorAccesos;
import controlador.ControladorEquipos;
import controlador.ControladorExpensas;
import controlador.ControladorReclamos;
import modelo.equipos.Equipo;
import modelo.personas.Vecino;
import modelo.lotes.Lote;
import modelo.reclamos.Reclamo;
import persistencia.Repositorio;
import persistencia.RepositorioArchivo;
import vista.VistaAccesos;
import vista.VistaEquipos;
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
        Repositorio<Equipo> equipos = new RepositorioArchivo<>("data/equipos.dat");
        ControladorReclamos controlador = new ControladorReclamos(vecinos, lotes, reclamos);
        ControladorExpensas controladorExpensas = new ControladorExpensas(lotes);
        ControladorEquipos controladorEquipos = new ControladorEquipos(equipos);
        ControladorAccesos controladorAccesos = new ControladorAccesos(vecinos, lotes);
        Scanner scanner = new Scanner(System.in);
        VistaReclamos vistaReclamos = new VistaReclamos(scanner, controlador);
        VistaExpensas vistaExpensas = new VistaExpensas(scanner, controladorExpensas);
        VistaEquipos vistaEquipos = new VistaEquipos(scanner, controladorEquipos);
        VistaAccesos vistaAccesos = new VistaAccesos(scanner, controladorAccesos);
        VistaPrincipal vista = new VistaPrincipal(scanner, vistaReclamos, vistaExpensas, vistaEquipos, vistaAccesos);
        vista.iniciar();
        scanner.close();
    }
}
