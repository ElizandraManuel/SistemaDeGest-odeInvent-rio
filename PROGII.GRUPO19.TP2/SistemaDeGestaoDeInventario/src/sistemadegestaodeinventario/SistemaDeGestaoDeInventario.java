package sistemadegestaodeinventario;

import sistemadegestaodeinventario.ui.Menu;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;


 
public class SistemaDeGestaoDeInventario {

    /**
     * Método principal - inicia o sistema
     * 
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        Menu menu = new Menu();
        menu.iniciar();
    }
}
