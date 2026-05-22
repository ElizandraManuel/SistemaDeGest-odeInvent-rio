package sistemadegestaodeinventario.ui;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import sistemadegestaodeinventario.modelo.Usuario;

/**
 * Menu básico do sistema.
 */
public class Menu {
    private final Scanner scanner;

    // Usuário autenticado
    private Usuario usuarioAutenticado = null;

    public Menu() {
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    public void iniciar() {
        boolean ativo = true;

        // Menu de autenticação
        while (usuarioAutenticado == null && ativo) {
            System.out.println("\n╔════════════════════════════════════════════╗");
            System.out.println("║        AUTENTICAÇÃO DE USUÁRIO           ║");
            System.out.println("╚════════════════════════════════════════════╝");
            System.out.println("1. Login");
            System.out.println("2. Cadastrar novo usuário");
            System.out.println("0. Sair");
            int op = lerInteiro("Escolha uma opção: ");
            switch (op) {
                case 1:
                    usuarioAutenticado = fazerLogin();
                    if (usuarioAutenticado == null) {
                        System.out.println("Credenciais inválidas. Tente novamente.");
                    }
                    break;
                case 2:
                    usuarioAutenticado = cadastrarUsuario();
                    if (usuarioAutenticado != null) {
                        System.out.println("Usuário cadastrado e autenticado com sucesso!");
                    }
                    break;
                case 0:
                    ativo = false;
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }

       
        while (ativo && usuarioAutenticado != null) {
            exibirMenuPrincipal();
            int opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    mostrarSubmenu("GESTÃO DE LOJAS", new String[] {
                        "1. Adicionar  Loja",
                        "2. Listar Todas as Lojas",
                        "3. Selecionar Loja",
                        "4. Ver Detalhes da Loja Ativa",
                        "0. Voltar ao Menu Principal"
                    });
                    break;
                case 2:
                    mostrarSubmenu("GESTÃO DE PRODUTOS", new String[] {
                        "1. Adicionar Produto à Loja",
                        "2. Listar Produtos da Loja",
                        "3. Consultar Stock de Produto",
                        "4. Aumentar Stock de Produto",
                        "5. Produtos com Stock Baixo",
                        "0. Voltar ao Menu Principal"
                    });
                    break;
                case 3:
                    mostrarSubmenu("REGISTAR VENDAS", new String[] {
                        "1. Iniciar Nova Venda",
                        "2. Ver Histórico de Vendas",
                        "3. Valor Total de Vendas",
                        "0. Voltar ao Menu Principal"
                    });
                    break;
                case 4:
                    mostrarSubmenu("RELATÓRIOS", new String[] {
                        "1. Relatório da Loja Atual",
                        "2. Inventário Completo",
                        "3. Relatório do Sistema",
                        "0. Voltar ao Menu Principal"
                    });
                    break;
                case 5:
                    mostrarSubmenu("CONFIGURAÇÃO", new String[] {
                        "1. Informações do Sistema",
                        "2. Criar Backup de Dados",
                        "3. Dados de Teste",
                        "0. Voltar ao Menu Principal"
                    });
                    break;
                case 0:
                    ativo = false;
                    System.out.println("\nSistema encerrado com sucesso.");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }

        scanner.close();
    }

   
    private Usuario usuarioCadastrado = null;

    private Usuario fazerLogin() {
        System.out.print("Email/Telefone: ");
        String credencial = scanner.nextLine().trim();
        System.out.print("Senha: ");
        String senha = scanner.nextLine().trim();
        if (usuarioCadastrado != null && usuarioCadastrado.autenticar(credencial, senha)) {
            return usuarioCadastrado;
        }
        return null;
    }

    private Usuario cadastrarUsuario() {
        String credencial;
        String senha;
        while (true) {
            System.out.print("Digite email ou número de telemóvel: ");
            credencial = scanner.nextLine().trim();
            if (credencial.isEmpty()) {
                System.out.println("O campo não pode estar em branco.");
                continue;
            }
            boolean isTelefone = credencial.matches("\\d{9}");
            boolean isEmail = credencial.contains("@");
            if (!isTelefone && !isEmail) {
                System.out.println("Digite um número de telemóvel válido (9 dígitos) ou um email válido (com '@').");
                continue;
            }
            break;
        }
        while (true) {
            System.out.print("Digite uma senha: ");
            senha = scanner.nextLine().trim();
            if (senha.isEmpty()) {
                System.out.println("A senha não pode estar em branco.");
                continue;
            }
            if (senha.length() < 8) {
                System.out.println("A senha deve ter pelo menos 8 caracteres.");
                continue;
            }
            break;
        }
        usuarioCadastrado = new sistemadegestaodeinventario.modelo.Usuario(credencial, senha);
        return usuarioCadastrado;
    }

    private void exibirMenuPrincipal() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  SISTEMA DE GESTÃO DE INVENTÁRIO - v1.0  ║");
        System.out.println("║  Programação 2 - 2025                    ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("┌────────── MENU PRINCIPAL ──────────┐");
        System.out.println("│ 1. Gestão de Lojas                │");
        System.out.println("│ 2. Gestão de Produtos             │");
        System.out.println("│ 3. Registar Vendas                │");
        System.out.println("│ 4. Relatórios e Consultas         │");
        System.out.println("│ 5. Configuração do Sistema        │");
        System.out.println("│ 0. Sair                           │");
        System.out.println("└───────────────────────────────────┘");
    }

    private void mostrarSubmenu(String titulo, String[] opcoes) {
        System.out.println();
        System.out.println("┌────────── " + titulo + " ──────────┐");
        for (String opcao : opcoes) {
            System.out.println("│ " + opcao);
        }
        System.out.println("└────────────────────────────────────┘");

        int escolha = lerInteiro("Escolha uma opção: ");
        if (escolha != 0) {
            System.out.println("Funcionalidade não implementada nesta versão.");
        }
    }

    private int lerInteiro(String prompt) {
        while (true) {
            System.out.print(prompt);
            String texto = scanner.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduza um número válido.");
            }
        }
    }
}