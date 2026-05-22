package sistemadegestaodeinventario.modelo;

public class Usuario {
    private String emailOuTelefone;
    private String senha;

    public Usuario(String emailOuTelefone, String senha) {
        this.emailOuTelefone = emailOuTelefone;
        this.senha = senha;
    }

    public String getEmailOuTelefone() {
        return emailOuTelefone;
    }

    public void setEmailOuTelefone(String emailOuTelefone) {
        this.emailOuTelefone = emailOuTelefone;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean autenticar(String credencial, String senha) {
        return this.emailOuTelefone.equals(credencial) && this.senha.equals(senha);
    }
}
