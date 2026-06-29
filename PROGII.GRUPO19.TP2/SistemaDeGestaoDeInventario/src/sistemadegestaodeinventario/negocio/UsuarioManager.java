package sistemadegestaodeinventario.negocio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sistemadegestaodeinventario.modelo.Usuario;

public class UsuarioManager {
    private List<Usuario> usuarios;

    public UsuarioManager() {
        this.usuarios = new ArrayList<>();
    }

    public void adicionarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O utilizador não pode ser nulo.");
        }
        if (buscarPorCredencial(usuario.getEmailOuTelefone()) != null) {
            throw new IllegalArgumentException("Já existe um utilizador com esta credencial.");
        }
        usuarios.add(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    public Usuario buscarPorCredencial(String credencial) {
        if (credencial == null) {
            return null;
        }
        String valor = credencial.trim();
        for (Usuario usuario : usuarios) {
            if (usuario.coincideCredencial(valor)) {
                return usuario;
            }
        }
        return null;
    }

    public Usuario autenticar(String credencial, String senha) {
        Usuario usuario = buscarPorCredencial(credencial);
        if (usuario != null && usuario.autenticar(credencial.trim(), senha)) {
            return usuario;
        }
        return null;
    }

    public boolean existeUsuarios() {
        return !usuarios.isEmpty();
    }

    public boolean removerUsuario(String credencial) {
        if (credencial == null) {
            return false;
        }
        String valor = credencial.trim();
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).coincideCredencial(valor)) {
                usuarios.remove(i);
                return true;
            }
        }
        return false;
    }

    public int contarUsuariosPorPerfil(Usuario.Perfil perfil) {
        int total = 0;
        for (Usuario usuario : usuarios) {
            if (usuario != null && usuario.getPerfil() == perfil) {
                total++;
            }
        }
        return total;
    }
}
