package sistemadegestaodeinventario.ui;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import sistemadegestaodeinventario.modelo.ItemVenda;
import sistemadegestaodeinventario.modelo.Loja;
import sistemadegestaodeinventario.modelo.Produto;
import sistemadegestaodeinventario.modelo.Usuario;
import sistemadegestaodeinventario.modelo.Venda;
import sistemadegestaodeinventario.negocio.InventarioManager;
import sistemadegestaodeinventario.negocio.LojaManager;
import sistemadegestaodeinventario.negocio.UsuarioManager;
import sistemadegestaodeinventario.persistencia.GestorFicheiros;
import sistemadegestaodeinventario.util.FormatadorMoeda;

public class Menu {
    private static final String ADMIN_PADRAO_CREDENCIAL = "admin@system";
    private static final String ADMIN_PADRAO_SENHA = "Admin1234";
    private static final int MENU_LARGURA = 86;
    private static final int MENU_MARGEM = 55;
    private static final String PROMPT_OPCAO = "Digite uma opcao: ";
    private static final String COR_ERRO = "\033[31m";
    private static final String COR_SUCESSO = "\033[32m";
    private static final String COR_RESET = "\033[0m";
    private final String os = System.getProperty("os.name");

    private final Scanner scanner;
    private final GestorFicheiros gestorFicheiros;
    private final UsuarioManager usuarioManager;
    private final InventarioManager inventarioManager;
    private final LojaManager lojaManager;
    private Usuario usuarioAutenticado = null;

    public Menu() {
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        this.gestorFicheiros = new GestorFicheiros();
        this.usuarioManager = new UsuarioManager();
        this.inventarioManager = new InventarioManager();
        this.lojaManager = new LojaManager(inventarioManager);
        carregarDadosIniciais();
    }

    public void iniciar() {
        boolean ativo = true;
        while (ativo && usuarioAutenticado == null) {
            int op = lerOpcaoMenu("AUTENTICACAO DE USUARIO",
                    "1. Login Vendedor",
                    "2. Login Gestor de Stock",
                    "3. Login Admin",
                    "0. Sair");
            switch (op) {
                case 1 -> fazerLoginVendedor();
                case 2 -> fazerLoginGestorStock();
                case 3 -> fazerLoginAdmin();
                case 0 -> ativo = false;
            }
        }
        while (ativo && usuarioAutenticado != null) {
            if (usuarioAutenticado.isAdmin()) {
                ativo = executarSessaoAdmin();
            } else if (usuarioAutenticado.isGestorStock()) {
                ativo = executarSessaoGestorStock();
            } else {
                ativo = executarSessaoVendedor();
            }
        }
        scanner.close();
    }

    private void carregarDadosIniciais() {
        // Reconstroi o estado do sistema a partir dos ficheiros guardados.
        for (Usuario usuario : gestorFicheiros.carregarUsuarios()) {
            try {
                usuarioManager.adicionarUsuario(usuario);
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (Loja loja : gestorFicheiros.carregarLojas()) {
            inventarioManager.adicionarLoja(loja);
        }
        if (!existeAdministrador()) {
            usuarioManager.adicionarUsuario(new Usuario(ADMIN_PADRAO_CREDENCIAL, ADMIN_PADRAO_SENHA, Usuario.Perfil.ADMIN));
            mostrarMensagem("Conta admin inicial criada: " + ADMIN_PADRAO_CREDENCIAL + " / " + ADMIN_PADRAO_SENHA);
        }
        gestorFicheiros.salvarUsuarios(usuarioManager.listarUsuarios());
    }

    private void salvarDados() {
        // Guarda os dados atualizados para manter a informação persistida.
        gestorFicheiros.salvarUsuarios(usuarioManager.listarUsuarios());
        gestorFicheiros.salvarLojas(inventarioManager.getLojasOrdenadas());
    }

    private boolean executarSessaoAdmin() {
        boolean ativo = true;
        clearTerminal();
        // Fluxo principal para utilizadores com perfil de administrador.
        while (ativo && usuarioAutenticado != null && usuarioAutenticado.isAdmin()) {
            int opcao = lerOpcaoMenuPrincipalAdmin();
            switch (opcao) {
                case 1 -> menuLojas();
                case 2 -> menuProdutos();
                case 3 -> menuVendas();
                case 4 -> menuRelatorios();
                case 5 -> menuConfiguracao();
                case 0 -> {
                    salvarDados();
                    usuarioAutenticado = null;
                    ativo = false;
                    mostrarSucesso("Sessao terminada com sucesso.");
                }
            }
        }
        clearTerminal();
        return ativo;
    }

    private boolean executarSessaoGestorStock() {
        boolean ativo = true;
        clearTerminal();
        // Fluxo principal para gestores de stock.
        while (ativo && usuarioAutenticado != null && usuarioAutenticado.isGestorStock()) {
            int opcao = lerOpcaoMenuPrincipalGestorStock();
            switch (opcao) {
                case 1 -> menuLojas();
                case 2 -> menuProdutos();
                case 3 -> menuVendas();
                case 4 -> menuRelatorios();
                case 5 -> menuConfiguracao();
                case 0 -> {
                    salvarDados();
                    usuarioAutenticado = null;
                    ativo = false;
                    mostrarSucesso("Sessao terminada com sucesso.");
                }
            }
        }
        clearTerminal();
        return ativo;
    }

    private boolean executarSessaoVendedor() {
        boolean ativo = true;
        if (!selecionarLojaDoVendedor()) {
            usuarioAutenticado = null;
            return false;
        }
        while (ativo && usuarioAutenticado != null && usuarioAutenticado.isVendedor()) {
            int opcao = lerOpcaoMenuPrincipalVendedor();
            switch (opcao) {
                case 1 -> consultarStockProduto();
                case 2 -> iniciarNovaVenda();
                case 3 -> verHistoricoMinhasVendas();
                case 0 -> {
                    salvarDados();
                    usuarioAutenticado = null;
                    ativo = false;
                    mostrarSucesso("Sessao terminada com sucesso.");
                }
            }
        }
        return ativo;
    }

    private void fazerLoginVendedor() {
        fazerLogin(Usuario.Perfil.VENDEDOR);
    }

    private void fazerLoginGestorStock() {
        fazerLogin(Usuario.Perfil.GESTOR_STOCK);
    }

    private void clearTerminal() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void exibirMenu(String titulo, String... opcoes) {
        String margem = margemMenu();
        System.out.println();
        if ("AUTENTICACAO DE USUARIO".equals(titulo)) {
            exibirBannerInicial(margem);
        }
        System.out.println(margem + "+" + "-".repeat(MENU_LARGURA - 2) + "+");
        System.out.println(margem + "|" + centralizar(titulo, MENU_LARGURA - 2) + "|");
        System.out.println(margem + "+" + "-".repeat(MENU_LARGURA - 2) + "+");
        for (String opcao : opcoes) {
            System.out.println(margem + "| " + alinharEsquerda(opcao, MENU_LARGURA - 4) + " |");
        }
        System.out.println(margem + "+" + "-".repeat(MENU_LARGURA - 2) + "+");
    }

    private void exibirBannerInicial(String margem) {
        System.out.println(margem + "=".repeat(MENU_LARGURA));
        System.out.println(margem + centralizar("SISTEMA DE GESTAO DE INVENTARIO (SGI)", MENU_LARGURA));
        System.out.println(margem + centralizar("Versao 1.0 - Gestao de Lojas, Stock e Vendas", MENU_LARGURA));
        System.out.println(margem + centralizar("Bem-vindo. Inicie sessao para continuar.", MENU_LARGURA));
        System.out.println(margem + "=".repeat(MENU_LARGURA));
        System.out.println();
    }

    private int lerOpcaoMenu(String titulo, String... opcoes) {
        exibirMenu(titulo, opcoes);
        Set<Integer> opcoesValidas = extrairOpcoesValidas(opcoes);
        while (true) {
            int opcao = lerInteiro(PROMPT_OPCAO);
            if (opcoesValidas.contains(opcao)) {
                return opcao;
            }
            mostrarErro("Opcao invalida.");
        }
    }

    private Set<Integer> extrairOpcoesValidas(String... opcoes) {
        Set<Integer> opcoesValidas = new HashSet<>();
        for (String opcao : opcoes) {
            int ponto = opcao.indexOf('.');
            if (ponto <= 0) {
                continue;
            }
            try {
                opcoesValidas.add(Integer.parseInt(opcao.substring(0, ponto).trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return opcoesValidas;
    }

    private String margemMenu() {
        return " ".repeat(MENU_MARGEM);
    }

    private String centralizar(String texto, int largura) {
        if (texto.length() >= largura) {
            return texto.substring(0, largura);
        }
        int espacosAntes = (largura - texto.length()) / 2;
        int espacosDepois = largura - texto.length() - espacosAntes;
        return " ".repeat(espacosAntes) + texto + " ".repeat(espacosDepois);
    }

    private String alinharEsquerda(String texto, int largura) {
        if (texto.length() >= largura) {
            return texto.substring(0, largura);
        }
        return texto + " ".repeat(largura - texto.length());
    }

    private void fazerLoginAdmin() {
        fazerLogin(Usuario.Perfil.ADMIN);
    }

    private void fazerLogin(Usuario.Perfil perfilEsperado) {
        // Valida as credenciais e permite apenas o acesso ao perfil pretendido.
        while (usuarioAutenticado == null) {
            String credencial = lerTexto("Email/Telefone (0 para voltar): ");
            if ("0".equals(credencial)) {
                clearTerminal();
                return;
            }

            if (!credencialValida(credencial)) {
                clearTerminal();
                mostrarErro(mensagemCredencialInvalida(credencial));
                continue;
            }

            Usuario usuario = usuarioManager.buscarPorCredencial(credencial);
            if (usuario == null) {
                clearTerminal();
                mostrarErro(mensagemCredencialNaoEncontrada(credencial));
                continue;
            }

            while (usuarioAutenticado == null) {
                String senha = lerTexto("Senha (0 para voltar): ");
                if ("0".equals(senha)) {
                    clearTerminal();
                    return;
                }

                clearTerminal();
                if (!usuario.senhaCorreta(senha)) {
                    mostrarErro("Senha incorreta para esta conta.");
                    continue;
                }

                if (usuario.getPerfil() != perfilEsperado) {
                    mostrarErro("Esta conta e " + nomePerfil(usuario.getPerfil()) + ". Use a opcao correta de login.");
                    return;
                }

                usuarioAutenticado = usuario;
                if (usuario.isVendedor() && !selecionarLojaDoVendedor()) {
                    usuarioAutenticado = null;
                    return;
                }
                mostrarSucesso("Login de " + nomePerfil(usuario.getPerfil()) + " efetuado com sucesso.");
            }
        }
    }

    private void mostrarErro(String mensagem) {
        System.out.println(margemMenu() + COR_ERRO + mensagem + COR_RESET);
    }

    private void mostrarSucesso(String mensagem) {
        System.out.println(margemMenu() + COR_SUCESSO + mensagem + COR_RESET);
    }

    private void mostrarMensagem(String mensagem) {
        for (String linha : mensagem.split("\\R", -1)) {
            System.out.println(margemMenu() + linha);
        }
    }

    private boolean credencialValida(String credencial) {
        return isEmail(credencial) || isTelefone(credencial);
    }

    private boolean isEmail(String credencial) {
        return credencial != null && credencial.contains("@");
    }

    private boolean isTelefone(String credencial) {
        return credencial != null && credencial.matches("\\d{9}");
    }

    private String mensagemCredencialInvalida(String credencial) {
        if (credencial != null && credencial.matches("\\d+")) {
            return "Numero de telemovel invalido: deve ter exatamente 9 digitos.";
        }
        return "Credencial invalida: digite um email com @ ou um numero de telemovel com 9 digitos.";
    }

    private String mensagemCredencialNaoEncontrada(String credencial) {
        if (isEmail(credencial)) {
            return "Email nao encontrado. Verifique se digitou o email corretamente.";
        }
        return "Numero de telemovel nao encontrado. Verifique se digitou os 9 digitos corretamente.";
    }

    private void criarUsuario() {
        String credencial;
        
        while (true) {
            credencial = lerTexto("Digite email ou numero de telemovel: ");
            boolean isTelefone = credencial.matches("\\d{9}");
            boolean isEmail = credencial.contains("@");
            if (!isTelefone && !isEmail) {
                mostrarErro("Digite um numero de telemovel valido (9 digitos) ou um email valido.");
                continue;
            }
            if (usuarioManager.buscarPorCredencial(credencial) != null) {
                mostrarErro("Ja existe um utilizador com esta credencial.");
                continue;
            }
            break;
        }
        String senha;
        while (true) {
            senha = lerTexto("Digite uma senha: ");
            if (senha.length() < 8) {
                mostrarErro("A senha deve ter pelo menos 8 caracteres.");
                continue;
            }
            break;
        }
        Usuario.Perfil perfil = lerPerfilUsuario("Perfil (ADMIN/GESTOR_STOCK/VENDEDOR): ");
        String idLoja = "";
        if (perfil == Usuario.Perfil.VENDEDOR) {
            if (inventarioManager.getLojasOrdenadas().isEmpty()) {
                mostrarErro("Cadastre uma loja antes de criar um vendedor.");
                return;
            }
            idLoja = lerLojaExistente("ID da loja do vendedor: ");
        }
        Usuario usuario = new Usuario(credencial, senha, perfil, idLoja);
        usuarioManager.adicionarUsuario(usuario);
        salvarDados();
        mostrarSucesso("Utilizador criado com sucesso.");
    }

    private int lerOpcaoMenuPrincipalAdmin() {
        return lerOpcaoMenu("MENU PRINCIPAL - ADMIN",
                "1. Gestao de Lojas",
                "2. Gestao de Produtos",
                "3. Registar Vendas",
                "4. Relatorios e Consultas",
                "5. Configuracao do Sistema",
                "0. Terminar Sessao");
    }

    private int lerOpcaoMenuPrincipalGestorStock() {
        return lerOpcaoMenu("MENU PRINCIPAL - GESTOR STOCK",
                "1. Gestao de Lojas",
                "2. Gestao de Produtos",
                "3. Registar Vendas",
                "4. Relatorios e Consultas",
                "5. Configuracao do Sistema",
                "0. Terminar Sessao");
    }

    private int lerOpcaoMenuPrincipalVendedor() {
        return lerOpcaoMenu("MENU PRINCIPAL - VENDEDOR",
                "1. Consultar Stock",
                "2. Registar Venda",
                "3. Historico das Minhas Vendas",
                "0. Terminar Sessao");
    }

    private void menuLojas() {
        clearTerminal();
        boolean voltar = false;
        while (!voltar) {
            switch (lerOpcaoMenu("GESTAO DE LOJAS",
                    "1. Adicionar Loja",
                    "2. Listar Todas as Lojas",
                    "3. Selecionar Loja",
                    "4. Ver Detalhes da Loja Ativa",
                    "5. Deletar Loja",
                    "6. Importar Loja via CSV",
                    "0. Voltar ao Menu Principal")) {
                case 1 -> adicionarLoja();
                case 2 -> listarLojas();
                case 3 -> selecionarLoja();
                case 4 -> verDetalhesLojaAtiva();
                case 5 -> deletarLoja();
                case 6 -> importarLojaCSV();
                case 0 -> voltar = true;
            }
        }
    }

    private void menuProdutos() {
        clearTerminal();
        boolean voltar = false;
        while (!voltar) {
            switch (lerOpcaoMenu("GESTAO DE PRODUTOS",
                    "1. Adicionar Produto a Loja",
                    "2. Listar Produtos da Loja",
                    "3. Consultar Stock de Produto",
                    "4. Aumentar Stock de Produto",
                    "5. Produtos com Stock Baixo",
                    "6. Deletar Produto",
                    "0. Voltar ao Menu Principal")) {
                case 1 -> adicionarProduto();
                case 2 -> listarProdutosDaLoja();
                case 3 -> consultarStockProduto();
                case 4 -> aumentarStockProduto();
                case 5 -> listarProdutosStockBaixo();
                case 6 -> deletarProduto();
                case 0 -> voltar = true;
            }
        }
    }

    private void menuVendas() {
        clearTerminal();
        boolean voltar = false;
        while (!voltar) {
            switch (lerOpcaoMenu("REGISTAR VENDAS",
                    "1. Iniciar Nova Venda",
                    "2. Ver Historico de Vendas",
                    "3. Valor Total de Vendas",
                    "0. Voltar ao Menu Principal")) {
                case 1 -> iniciarNovaVenda();
                case 2 -> verHistoricoVendas();
                case 3 -> verValorTotalVendas();
                case 0 -> voltar = true;
            }
        }
    }

    private void menuRelatorios() {
        clearTerminal();
        boolean voltar = false;
        while (!voltar) {
            switch (lerOpcaoMenu("RELATORIOS",
                    "1. Relatorio da Loja Atual",
                    "2. Inventario Completo",
                    "3. Relatorio do Sistema",
                    "0. Voltar ao Menu Principal")) {
                case 1 -> mostrarMensagem(lojaManager.obterRelatorioLoja());
                case 2 -> relatorioInventarioCompleto();
                case 3 -> mostrarMensagem(inventarioManager.obterRelatorioSistema());
                case 0 -> voltar = true;
            }
        }
    }

    private void menuConfiguracao() {
        clearTerminal();
        boolean voltar = false;
        while (!voltar) {
            if (usuarioAutenticado.isAdmin()) {
                switch (lerOpcaoMenu("CONFIGURACAO",
                        "1. Informacoes do Sistema",
                        "2. Criar Backup de Dados",
                        "3. Dados de Teste",
                        "4. Gestao de Utilizadores",
                        "0. Voltar ao Menu Principal")) {
                    case 1 -> mostrarInformacoesSistema();
                    case 2 -> { gestorFicheiros.criarBackup(); mostrarSucesso("Backup criado com sucesso."); }
                    case 3 -> inserirDadosTeste();
                    case 4 -> menuUsuarios();
                    case 0 -> voltar = true;
                }
            } else {
                switch (lerOpcaoMenu("CONFIGURACAO",
                        "1. Informacoes do Sistema",
                        "2. Criar Backup de Dados",
                        "3. Dados de Teste",
                        "0. Voltar ao Menu Principal")) {
                    case 1 -> mostrarInformacoesSistema();
                    case 2 -> { gestorFicheiros.criarBackup(); mostrarSucesso("Backup criado com sucesso."); }
                    case 3 -> inserirDadosTeste();
                    case 0 -> voltar = true;
                }
            }
        }
    }

    private void menuUsuarios() {
        clearTerminal();
        if (!usuarioAutenticado.isAdmin()) {
            mostrarErro("Acesso restrito a administradores.");
            return;
        }
        boolean voltar = false;
        while (!voltar) {
            switch (lerOpcaoMenu("GESTAO DE UTILIZADORES",
                    "1. Criar Novo Utilizador",
                    "2. Listar Utilizadores",
                    "3. Eliminar Utilizador",
                    "0. Voltar")) {
                case 1 -> criarUsuario();
                case 2 -> listarUsuarios();
                case 3 -> eliminarUsuario();
                case 0 -> voltar = true;
            }
        }
    }

    private void listarUsuarios() {
        
        for (Usuario usuario : usuarioManager.listarUsuarios()) {
            mostrarMensagem(usuario.toString());
        }
    }

    private void eliminarUsuario() {
        String credencial = lerTexto("Credencial do utilizador a eliminar: ");
        if (credencial.equals(usuarioAutenticado.getEmailOuTelefone())) {
            mostrarErro("Nao e possivel eliminar a sua propria conta.");
            return;
        }

        Usuario usuario = usuarioManager.buscarPorCredencial(credencial);
        if (usuario == null) {
            mostrarErro("Utilizador nao encontrado.");
            return;
        }

        if (usuario.isAdmin() && usuarioManager.contarUsuariosPorPerfil(Usuario.Perfil.ADMIN) <= 1) {
            mostrarErro("Nao e possivel eliminar o ultimo administrador do sistema.");
            return;
        }

        if (!lerConfirmacao("Confirma a eliminacao de " + credencial + "? (S/N): ")) {
            mostrarMensagem("Eliminacao cancelada.");
            return;
        }

        if (usuarioManager.removerUsuario(credencial)) {
            salvarDados();
            mostrarSucesso("Utilizador eliminado com sucesso.");
        } else {
            mostrarErro("Nao foi possivel eliminar o utilizador.");
        }
    }

    private void adicionarLoja() {
        
        String id = lerTexto("ID da loja: ");
        if (inventarioManager.existeLoja(id)) { mostrarErro("Ja existe uma loja com este ID."); return; }
        String nome = lerTexto("Nome da loja: ");
        String morada = lerTexto("Morada: ");
        String telefone = lerTexto("Numero de telefone: ");
        inventarioManager.adicionarLoja(new Loja(id, nome, morada, telefone));
        salvarDados();
        mostrarSucesso("Loja adicionada com sucesso.");
    }

    private void listarLojas() {
        
        List<Loja> lojas = inventarioManager.getLojasOrdenadas();
        if (lojas.isEmpty()) { mostrarMensagem("Nenhuma loja registada."); return; }
        for (Loja loja : lojas) mostrarMensagem(loja.toString());
    }

    private void selecionarLoja() {
        
        String id = lerTexto("ID da loja a selecionar: ");
        if (lojaManager.selecionarLoja(id)) mostrarSucesso("Loja selecionada com sucesso: " + lojaManager.getLojaAtual().getNome());
        else mostrarErro("Loja nao encontrada.");
    }

    private void verDetalhesLojaAtiva() {
        
        if (lojaManager.getLojaAtual() == null) { mostrarMensagem("Nenhuma loja selecionada."); return; }
        mostrarMensagem(lojaManager.getLojaAtual().toString());
        mostrarMensagem("Produtos: " + lojaManager.getLojaAtual().listarProdutos().size());
        mostrarMensagem("Vendas: " + lojaManager.getLojaAtual().listarVendas().size());
        mostrarMensagem("Total vendido: " + FormatadorMoeda.kz(lojaManager.getLojaAtual().obterTotalVendas()));
    }

    private void adicionarProduto() {
        
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        Loja atual = lojaManager.getLojaAtual();
        Produto produto = new Produto(lerTexto("Codigo do produto: "), lerTexto("Nome: "), lerTexto("Descricao: "), lerDouble("Preco: "), lerInteiro("Quantidade em stock: "), lerInteiro("Quantidade minima permitida: "));
        atual.adicionarProduto(produto);
        salvarDados();
        mostrarSucesso("Produto adicionado com sucesso.");
    }

    private void listarProdutosDaLoja() {
        
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        if (loja.listarProdutos().isEmpty()) { mostrarMensagem("Sem produtos registados."); return; }
        for (Produto produto : loja.listarProdutos()) mostrarMensagem(produto.toString());
    }

    private void consultarStockProduto() {
        
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        Produto produto = loja.consultarProduto(lerTexto("ID do produto: "));
        if (produto == null) { mostrarErro("Produto nao encontrado."); return; }
        mostrarMensagem("Stock atual: " + produto.getQuantidadeEmStock());
    }

    private void aumentarStockProduto() {
        
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        Produto produto = loja.consultarProduto(lerTexto("ID do produto: "));
        if (produto == null) { mostrarErro("Produto nao encontrado."); return; }
        produto.aumentarStock(lerInteiro("Quantidade a adicionar: "));
        salvarDados();
        mostrarSucesso("Stock atualizado com sucesso.");
    }

    private void listarProdutosStockBaixo() {
        
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        boolean encontrado = false;
        for (Produto produto : loja.listarProdutos()) {
            if (produto.estaAbaixoMinimo()) { mostrarMensagem(produto.toString()); encontrado = true; }
        }
        if (!encontrado) mostrarMensagem("Nenhum produto com stock baixo.");
    }

    private void iniciarNovaVenda() {
        clearTerminal();
        // Cria uma nova venda a partir dos produtos selecionados pela loja ativa.
        Loja loja = lojaManager.getLojaAtual();
        if (usuarioAutenticado.isVendedor()) {
            if (!selecionarLojaDoVendedor()) {
                return;
            }
            loja = lojaManager.getLojaAtual();
        } else if (loja == null) {
            String idLoja = lerTexto("ID da loja: ");
            if (!lojaManager.selecionarLoja(idLoja)) {
                mostrarErro("Loja nao encontrada.");
                return;
            }
            loja = lojaManager.getLojaAtual();
        }
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        if (loja.listarProdutos().isEmpty()) { mostrarMensagem("A loja nao possui produtos."); return; }
        List<ItemVenda> itens = new ArrayList<>();
        while (true) {
            String idProduto = lerTexto("ID do produto: ");
            Produto produto = loja.consultarProduto(idProduto);
            if (produto == null) { mostrarErro("Produto nao encontrado."); continue; }
            int quantidadeSelecionada = quantidadeSelecionadaNaVenda(itens, produto.getIdProduto());
            int stockDisponivel = produto.getQuantidadeEmStock() - quantidadeSelecionada;
            mostrarMensagem("Produto: " + produto.getNome() + " | Stock disponivel: " + stockDisponivel);
            int quantidade = lerInteiroPositivo("Quantidade a vender: ");
            if (quantidade > stockDisponivel) {
                mostrarErro("Stock insuficiente. Quantidade disponivel para esta venda: " + stockDisponivel);
                continue;
            }
            adicionarItemVenda(itens, produto, quantidade);
            mostrarMensagem("Item adicionado a venda.");
            if (!lerConfirmacao("Adicionar outro produto a esta venda? (S/N): ")) {
                break;
            }
        }
        if (itens.isEmpty()) { mostrarMensagem("Venda cancelada."); return; }
        try {
            String idVendedor = usuarioAutenticado != null && usuarioAutenticado.isVendedor()
                    ? usuarioAutenticado.getEmailOuTelefone()
                    : "";
            Venda venda = inventarioManager.registarVenda(loja.getIdLoja(), itens, idVendedor);
            salvarDados();
            mostrarSucesso("Venda registada com sucesso: " + venda.getIdVenda());
            mostrarMensagem("Total: " + FormatadorMoeda.kz(venda.getTotalVenda()));
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        }
    }

    private int quantidadeSelecionadaNaVenda(List<ItemVenda> itens, String idProduto) {
        int total = 0;
        for (ItemVenda item : itens) {
            if (item.idProduto().equals(idProduto)) {
                total += item.quantidade();
            }
        }
        return total;
    }

    private void adicionarItemVenda(List<ItemVenda> itens, Produto produto, int quantidade) {
        for (int i = 0; i < itens.size(); i++) {
            ItemVenda item = itens.get(i);
            if (item.idProduto().equals(produto.getIdProduto())) {
                int novaQuantidade = item.quantidade() + quantidade;
                itens.set(i, new ItemVenda(produto.getIdProduto(), produto.getNome(), novaQuantidade, produto.getPreco()));
                return;
            }
        }
        itens.add(new ItemVenda(produto.getIdProduto(), produto.getNome(), quantidade, produto.getPreco()));
    }

    private void verHistoricoVendas() {
        
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        if (loja.listarVendas().isEmpty()) { mostrarMensagem("Sem vendas registadas."); return; }
        for (Venda venda : loja.listarVendas()) {
            mostrarMensagem(venda.toString());
            for (ItemVenda item : venda.getItensVenda()) mostrarMensagem("   - " + item);
        }
    }

    private void verHistoricoMinhasVendas() {
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        if (usuarioAutenticado == null || !usuarioAutenticado.isVendedor()) {
            mostrarErro("Opcao disponivel apenas para vendedores.");
            return;
        }

        String idVendedor = usuarioAutenticado.getEmailOuTelefone();
        boolean encontrou = false;
        double total = 0.0;
        for (Venda venda : loja.listarVendas()) {
            if (!idVendedor.equals(venda.getIdVendedor())) {
                continue;
            }
            encontrou = true;
            total += venda.getTotalVenda();
            mostrarMensagem(venda.toString());
            for (ItemVenda item : venda.getItensVenda()) mostrarMensagem("   - " + item);
        }

        if (!encontrou) {
            mostrarMensagem("Ainda nao existem vendas registadas por este vendedor.");
            return;
        }
        mostrarMensagem("Total das minhas vendas: " + FormatadorMoeda.kz(total));
    }

    private void verValorTotalVendas() {
        
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        mostrarMensagem("Valor total vendido: " + FormatadorMoeda.kz(loja.obterTotalVendas()));
    }

    private void relatorioInventarioCompleto() {
        
        List<Loja> lojas = inventarioManager.getLojasOrdenadas();
        if (lojas.isEmpty()) { mostrarMensagem("Nenhuma loja registada."); return; }
        for (Loja loja : lojas) {
            mostrarMensagem("=== " + loja.getNome() + " ===");
            for (Produto produto : loja.listarProdutos()) mostrarMensagem(produto.toString());
        }
    }

    private void mostrarInformacoesSistema() {
        
        mostrarMensagem("Nome do sistema: Sistema de Gestao de Inventario");
        mostrarMensagem("Versao: v1.0");
        mostrarMensagem("Disciplina: Programacao II");
        mostrarMensagem("Ano lectivo: 2025/2026");
        mostrarMensagem("Integridade de dados: " + (gestorFicheiros.verificarIntegridade() ? "OK" : "ERRO"));
    }

    private void inserirDadosTeste() {
        
        if (!inventarioManager.existeLoja("L001")) {
            Loja loja = new Loja("L001", "Loja Centro", "Rua Principal, 10", "211234567");
            loja.adicionarProduto(new Produto("P001", "Arroz 1kg", "Arroz branco", 1000, 25, 5));
            loja.adicionarProduto(new Produto("P002", "Oleo 1L", "Oleo alimentar", 1500, 18, 4));
            inventarioManager.adicionarLoja(loja);
        }
        if (!inventarioManager.existeLoja("L002")) {
            Loja loja = new Loja("L002", "Loja Norte", "Av. da Republica, 50", "218765432");
            loja.adicionarProduto(new Produto("P003", "Acucar 1kg", "Acucar branco", 800, 40, 8));
            inventarioManager.adicionarLoja(loja);
        }
        salvarDados();
        mostrarSucesso("Dados de teste inseridos com sucesso.");
    }

    private int lerInteiro(String prompt) {
        
        while (true) {
            System.out.print(margemMenu() + prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                mostrarErro("Por favor, introduza um numero valido.");
            }
        }
    }

    private int lerInteiroPositivo(String prompt) {
        while (true) {
            int valor = lerInteiro(prompt);
            if (valor > 0) {
                return valor;
            }
            mostrarErro("A quantidade deve ser maior que zero.");
        }
    }

    private double lerDouble(String prompt) {
        
        while (true) {
            System.out.print(margemMenu() + prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                mostrarErro("Por favor, introduza um valor numerico valido.");
            }
        }
    }

    private void deletarProduto() {
        
        Loja loja = lojaManager.getLojaAtual();
        if (loja == null) { mostrarErro("Selecione uma loja primeiro."); return; }
        String idProduto = lerTexto("ID do produto a deletar: ");
        Produto produto = loja.consultarProduto(idProduto);
        if (produto == null) { mostrarErro("Produto nao encontrado."); return; }
        lojaManager.deletarProduto(idProduto);
        salvarDados();
        mostrarSucesso("Produto deletado com sucesso.");
    }

    private void deletarLoja() {
        
        String idLoja = lerTexto("ID da loja a deletar: ");
        if (existeVendedorAssociadoLoja(idLoja)) {
            mostrarErro("Nao e possivel deletar a loja. Existe vendedor associado a esta loja.");
            return;
        }
        if (lojaManager.deletarLoja(idLoja)) {
            gestorFicheiros.deletarDadosLoja(idLoja);
            salvarDados();
            mostrarSucesso("Loja deletada com sucesso.");
        } else {
            mostrarErro("Loja nao encontrada.");
        }
    }

    private void importarLojaCSV() {
        
        String caminho = lerTexto("Caminho do ficheiro CSV: ");
        try {
            Loja loja = gestorFicheiros.importarLojaCSV(caminho);
            if (inventarioManager.existeLoja(loja.getIdLoja())) {
                mostrarErro("Ja existe uma loja com este ID: " + loja.getIdLoja());
                return;
            }
            inventarioManager.adicionarLoja(loja);
            salvarDados();
            mostrarSucesso("Loja importada com sucesso: " + loja.getNome());
        } catch (IllegalArgumentException e) {
            mostrarErro("Erro ao importar CSV: " + e.getMessage());
        }
    }

    private String lerTexto(String prompt) {
        
        while (true) {
            System.out.print(margemMenu() + prompt);
            String texto = scanner.nextLine().trim();
            if (texto.isEmpty()) {
                mostrarErro("O campo nao pode estar em branco.");
                continue;
            }
            return texto;
        }
    }

    private boolean lerConfirmacao(String prompt) {
        while (true) {
            String resposta = lerTexto(prompt).toUpperCase();
            if ("S".equals(resposta) || "SIM".equals(resposta)) {
                return true;
            }
            if ("N".equals(resposta) || "NAO".equals(resposta) || "NO".equals(resposta)) {
                return false;
            }
            mostrarErro("Resposta invalida. Use S ou N.");
        }
    }

    private Usuario.Perfil lerPerfilUsuario(String prompt) {
        while (true) {
            String valor = lerTexto(prompt).toUpperCase();
            if ("ADMIN".equals(valor)) {
                return Usuario.Perfil.ADMIN;
            }
            if ("GESTOR".equals(valor) || "GESTOR_STOCK".equals(valor) || "GESTOR DE STOCK".equals(valor)) {
                return Usuario.Perfil.GESTOR_STOCK;
            }
            if ("VENDEDOR".equals(valor) || "USUARIO".equals(valor) || "UTILIZADOR".equals(valor)) {
                return Usuario.Perfil.VENDEDOR;
            }
            mostrarErro("Perfil invalido. Use ADMIN, GESTOR_STOCK ou VENDEDOR.");
        }
    }

    private String lerLojaExistente(String prompt) {
        while (true) {
            String idLoja = lerTexto(prompt);
            if (inventarioManager.existeLoja(idLoja)) {
                return idLoja;
            }
            mostrarErro("Loja nao encontrada. Cadastre ou informe uma loja existente.");
        }
    }

    private boolean selecionarLojaDoVendedor() {
        if (usuarioAutenticado == null || !usuarioAutenticado.isVendedor()) {
            return true;
        }
        String idLoja = usuarioAutenticado.getIdLoja();
        if (idLoja == null || idLoja.isBlank()) {
            mostrarErro("Este vendedor nao esta associado a nenhuma loja.");
            return false;
        }
        if (!lojaManager.selecionarLoja(idLoja)) {
            mostrarErro("A loja associada ao vendedor nao foi encontrada: " + idLoja);
            return false;
        }
        return true;
    }

    private String nomePerfil(Usuario.Perfil perfil) {
        if (perfil == Usuario.Perfil.ADMIN) {
            return "administrador";
        }
        if (perfil == Usuario.Perfil.GESTOR_STOCK) {
            return "gestor de stock";
        }
        return "vendedor";
    }

    private boolean existeVendedorAssociadoLoja(String idLoja) {
        for (Usuario usuario : usuarioManager.listarUsuarios()) {
            if (usuario.isVendedor() && usuario.getIdLoja().equals(idLoja)) {
                return true;
            }
        }
        return false;
    }

    private boolean existeAdministrador() {
        for (Usuario usuario : usuarioManager.listarUsuarios()) {
            if (usuario.isAdmin()) {
                return true;
            }
        }
        return false;
    }
}


