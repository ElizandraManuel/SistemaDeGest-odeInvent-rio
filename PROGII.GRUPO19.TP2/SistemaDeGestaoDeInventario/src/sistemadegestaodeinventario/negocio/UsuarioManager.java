package sistemadegestaodeinventario.negocio;

import java.util.ArrayList;
import java.util.List;
import sistemadegestaodeinventario.modelo.Usuario;

public class UsuarioManager {
    private List<Usuario> usuarios;

    public UsuarioManager() {
        this.usuarios = new ArrayList<>();
    }

    public void adicionarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarios;
    }

   
}
