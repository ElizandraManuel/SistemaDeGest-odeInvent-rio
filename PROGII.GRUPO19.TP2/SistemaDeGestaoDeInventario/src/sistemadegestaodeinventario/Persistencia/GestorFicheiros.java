ackage sistemadegestaodeinventario.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sistemadegestaodeinventario.modelo.ItemVenda;
import sistemadegestaodeinventario.modelo.Loja;
import sistemadegestaodeinventario.modelo.Produto;
import sistemadegestaodeinventario.modelo.Usuario;
import sistemadegestaodeinventario.modelo.Venda;
import sistemadegestaodeinventario.util.FormatadorMoeda;

public class GestorFicheiros {

	private static final char DELIMITADOR_CSV_EXCEL = ';';
	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter FORMATO_DATA_CURTA = DateTimeFormatter.ofPattern("dd/MM/yy");
	private static final DateTimeFormatter FORMATO_DATA_ANTIGO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
	private final Path dataDir = Paths.get("data");

	public GestorFicheiros() {
		try {
			Files.createDirectories(dataDir);
		} catch (IOException e) {
			throw new IllegalStateException("Não foi possível criar a pasta de dados.", e);
		}
	}

	public List<Usuario> carregarUsuarios() {
		Path ficheiro = dataDir.resolve("usuarios.txt");
		List<Usuario> usuarios = new ArrayList<>();
		if (!Files.exists(ficheiro)) {
			return usuarios;
		}
		try {
			for (String linha : Files.readAllLines(ficheiro, StandardCharsets.UTF_8)) {
				if (linha.isBlank()) {
					continue;
				}
				String[] partes = linha.split("\\|", -1);
				if (partes.length >= 2) {
					Usuario.Perfil perfil = partes.length >= 3 ? lerPerfil(partes[2]) : Usuario.Perfil.VENDEDOR;
					String idLoja = partes.length >= 4 ? partes[3] : "";
					usuarios.add(new Usuario(partes[0], partes[1], perfil, idLoja));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao carregar utilizadores.", e);
		}
		return usuarios;
	}

	public void salvarUsuarios(List<Usuario> usuarios) {
		Path ficheiro = dataDir.resolve("usuarios.txt");
		List<String> linhas = new ArrayList<>();
		for (Usuario usuario : usuarios) {
			linhas.add(String.join("|",
				usuario.getEmailOuTelefone(),
				usuario.getSenha(),
				usuario.getPerfil().name(),
				usuario.getIdLoja()));
		}
		escreverLinhas(ficheiro, linhas);
	}

	public List<Loja> carregarLojas() {
		Path ficheiro = dataDir.resolve("lojas.txt");
		List<Loja> lojas = new ArrayList<>();
		if (!Files.exists(ficheiro)) {
			return lojas;
		}
		try {
			for (String linha : Files.readAllLines(ficheiro, StandardCharsets.UTF_8)) {
				if (linha.isBlank()) {
					continue;
				}
				String[] partes = linha.split("\\|", -1);
				if (partes.length >= 4) {
					Loja loja = new Loja(partes[0], partes[1], partes[2], partes[3]);
					carregarProdutos(loja);
					List<Venda> vendas = carregarVendas(loja.getIdLoja());
					for (Venda venda : vendas) {
						loja.registarVenda(venda);
					}
					lojas.add(loja);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao carregar lojas.", e);
		}
		return lojas;
	}

	public void salvarLojas(List<Loja> lojas) {
		Path ficheiro = dataDir.resolve("lojas.txt");
		List<String> linhas = new ArrayList<>();
		for (Loja loja : lojas) {
			linhas.add(String.join("|",
				loja.getIdLoja(), loja.getNome(), loja.getEndereco(), loja.getTelefone()));
			salvarProdutos(loja);
			salvarVendas(loja.getIdLoja(), loja.listarVendas());
		}
		escreverLinhas(ficheiro, linhas);
	}

	public void deletarDadosLoja(String idLoja) {
		if (idLoja == null || idLoja.trim().isEmpty()) {
			return;
		}
		try {
			Files.deleteIfExists(dataDir.resolve(nomeFicheiroProdutos(idLoja)));
			Files.deleteIfExists(dataDir.resolve(nomeFicheiroVendas(idLoja)));
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao deletar ficheiros da loja: " + idLoja, e);
		}
	}

	public void salvarProdutos(Loja loja) {
		Path ficheiro = dataDir.resolve(nomeFicheiroProdutos(loja.getIdLoja()));
		List<String> linhas = new ArrayList<>();
		for (Produto produto : loja.listarProdutos()) {
			linhas.add(String.join("|",
				produto.getIdProduto(),
				produto.getNome(),
				produto.getDescricao(),
				String.valueOf(produto.getPreco()),
				String.valueOf(produto.getQuantidadeEmStock()),
				String.valueOf(produto.getQuantidadeMinima())));
		}
		escreverLinhas(ficheiro, linhas);
	}

	public void carregarProdutos(Loja loja) {
		Path ficheiro = dataDir.resolve(nomeFicheiroProdutos(loja.getIdLoja()));
		if (!Files.exists(ficheiro)) {
			return;
		}
		try {
			for (String linha : Files.readAllLines(ficheiro, StandardCharsets.UTF_8)) {
				if (linha.isBlank()) {
					continue;
				}
				String[] partes = linha.split("\\|", -1);
				if (partes.length >= 6) {
					loja.adicionarProduto(new Produto(
						partes[0],
						partes[1],
						partes[2],
						Double.parseDouble(partes[3]),
						Integer.parseInt(partes[4]),
						Integer.parseInt(partes[5])));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao carregar produtos.", e);
		}
	}

	public void salvarVendas(String idLoja, List<Venda> vendas) {
		Path ficheiro = dataDir.resolve(nomeFicheiroVendas(idLoja));
		List<String> linhas = new ArrayList<>();
		linhas.add(juntarCsv("ID_VENDA", "DATA_VENDA", "HORA_VENDA", "ID_LOJA", "ID_VENDEDOR", "ID_PRODUTO", "NOME_PRODUTO", "QUANTIDADE", "PRECO_UNITARIO", "SUBTOTAL"));
		for (Venda venda : vendas) {
			for (ItemVenda item : venda.getItensVenda()) {
				linhas.add(juntarCsv(
					venda.getIdVenda(),
					formatarDataCsvExcel(venda.getDataVenda()),
					venda.getDataVenda().format(FORMATO_HORA),
					venda.getIdLoja(),
					venda.getIdVendedor(),
					item.idProduto(),
					item.nomeProduto(),
					String.valueOf(item.quantidade()),
					formatarNumeroCsv(item.precoUnitario()),
					formatarNumeroCsv(item.subtotal())));
			}
			linhas.add(juntarCsv(
				"",
				"",
				"",
				"",
				"",
				"",
				"TOTAL:",
				"",
				"",
				FormatadorMoeda.kz(venda.getTotalVenda())));
		}
		escreverLinhas(ficheiro, linhas);
	}

	public List<Venda> carregarVendas(String idLoja) {
		Path ficheiro = dataDir.resolve(nomeFicheiroVendas(idLoja));
		List<Venda> vendas = new ArrayList<>();
		if (!Files.exists(ficheiro)) {
			return vendas;
		}
		try {
			Map<String, Venda> porId = new HashMap<>();
			List<String> linhas = Files.readAllLines(ficheiro, StandardCharsets.UTF_8);
			for (int i = 1; i < linhas.size(); i++) {
				String linha = linhas.get(i);
				if (linha.isBlank()) {
					continue;
				}
				String[] partes = separarCsv(linha);
				if (partes.length >= 10 && partes[6].trim().toUpperCase().startsWith("TOTAL")) {
					continue;
				}
				if (partes.length >= 9 && partes[5].trim().toUpperCase().startsWith("TOTAL")) {
					continue;
				}
				if (partes.length >= 10) {
					Venda venda = obterOuCriarVenda(porId, partes[0], lerDataHoraVenda(partes[1], partes[2]), partes[3], partes[4]);
					venda.adicionarItem(new ItemVenda(
						partes[5],
						partes[6],
						Integer.parseInt(partes[7]),
						lerNumeroCsv(partes[8])));
				} else if (partes.length >= 9) {
					Venda venda = obterOuCriarVenda(porId, partes[0], lerDataHoraVenda(partes[1], partes[2]), partes[3], "");
					venda.adicionarItem(new ItemVenda(
						partes[4],
						partes[5],
						Integer.parseInt(partes[6]),
						lerNumeroCsv(partes[7])));
				} else if (partes.length >= 8) {
					Venda venda = obterOuCriarVenda(porId, partes[0], LocalDateTime.parse(partes[1]), partes[2], "");
					venda.adicionarItem(new ItemVenda(
						partes[3],
						partes[4],
						Integer.parseInt(partes[5]),
						lerNumeroCsv(partes[6])));
				}
			}
			vendas.addAll(porId.values());
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao carregar vendas.", e);
		}
		return vendas;
	}

	private LocalDateTime lerDataHoraVenda(String data, String hora) {
		data = normalizarTextoExcel(data);
		LocalDate dataVenda;
		if (data.contains("/")) {
			DateTimeFormatter formato = data.length() == 8 ? FORMATO_DATA_CURTA : FORMATO_DATA;
			dataVenda = LocalDate.parse(data, formato);
		} else {
			dataVenda = LocalDate.parse(data, FORMATO_DATA_ANTIGO);
		}
		return LocalDateTime.of(dataVenda, LocalTime.parse(hora, FORMATO_HORA));
	}

	private String formatarDataCsvExcel(LocalDateTime dataVenda) {
		return "=\"" + dataVenda.format(FORMATO_DATA) + "\"";
	}

	private String normalizarTextoExcel(String valor) {
		String texto = valor == null ? "" : valor.trim();
		if (texto.startsWith("=\"") && texto.endsWith("\"") && texto.length() >= 3) {
			return texto.substring(2, texto.length() - 1);
		}
		if (texto.startsWith("=")) {
			texto = texto.substring(1).trim();
			if (texto.startsWith("\"") && texto.endsWith("\"") && texto.length() >= 2) {
				return texto.substring(1, texto.length() - 1);
			}
			return texto;
		}
		if (texto.startsWith("'")) {
			return texto.substring(1);
		}
		return texto;
	}

	private Venda obterOuCriarVenda(Map<String, Venda> vendas, String idVenda, LocalDateTime dataVenda, String idLoja, String idVendedor) {
		Venda venda = vendas.get(idVenda);
		if (venda == null) {
			venda = new Venda(idVenda, dataVenda, idLoja, idVendedor);
			vendas.put(idVenda, venda);
		} else if ((venda.getIdVendedor() == null || venda.getIdVendedor().isBlank()) && idVendedor != null && !idVendedor.isBlank()) {
			venda.setIdVendedor(idVendedor);
		}
		return venda;
	}

	public Loja importarLojaCSV(String caminhoFicheiro) {
		Path ficheiro = Paths.get(caminhoFicheiro);
		if (!Files.exists(ficheiro)) {
			throw new IllegalArgumentException("Ficheiro nao encontrado: " + caminhoFicheiro);
		}
		try {
			List<String> linhas = Files.readAllLines(ficheiro, StandardCharsets.UTF_8);
			int index = 0;
			while (index < linhas.size() && linhas.get(index).isBlank()) {
				index++;
			}
			if (index >= linhas.size()) {
				throw new IllegalArgumentException("Ficheiro CSV vazio ou sem conteudo util.");
			}
			String cabecalho = linhas.get(index).trim();
			String[] partesLoja = separarCsv(cabecalho);
			if (partesLoja.length < 4) {
				throw new IllegalArgumentException("Cabecalho da loja invalido. Formato esperado: id;nome;morada;telefone");
			}
			Loja loja = new Loja(partesLoja[0].trim(), partesLoja[1].trim(), partesLoja[2].trim(), partesLoja[3].trim());
			for (int i = index + 1; i < linhas.size(); i++) {
				String linha = linhas.get(i).trim();
				if (linha.isBlank()) {
					continue;
				}
				String[] p = separarCsv(linha);
				if (p.length < 6) {
					throw new IllegalArgumentException("Linha de produto invalida na linha " + (i + 1) + ". Formato esperado: id;nome;descricao;preco;quantidade;quantidadeMinima");
				}
				Produto produto = new Produto(
					p[0].trim(),
					p[1].trim(),
					p[2].trim(),
					lerNumeroCsv(p[3].trim()),
					Integer.parseInt(p[4].trim()),
					Integer.parseInt(p[5].trim()));
				loja.adicionarProduto(produto);
			}
			return loja;
		} catch (IOException e) {
			throw new IllegalArgumentException("Erro ao ler o ficheiro: " + e.getMessage(), e);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Erro ao converter numeros no ficheiro CSV: " + e.getMessage(), e);
		}
	}

	public boolean verificarIntegridade() {
		return Files.exists(dataDir) && Files.isDirectory(dataDir);
	}

	public void criarBackup() {
		Path backupDir = dataDir.resolve("backup");
		try {
			Files.createDirectories(backupDir);
			copiarSeExistir(dataDir.resolve("lojas.txt"), backupDir.resolve("lojas.txt"));
			copiarSeExistir(dataDir.resolve("usuarios.txt"), backupDir.resolve("usuarios.txt"));
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao criar backup.", e);
		}
	}

	private Usuario.Perfil lerPerfil(String valor) {
		if (valor == null) {
			return Usuario.Perfil.VENDEDOR;
		}
		String perfil = valor.trim().toUpperCase();
		if ("NORMAL".equals(perfil) || "USUARIO".equals(perfil) || "UTILIZADOR".equals(perfil)) {
			return Usuario.Perfil.VENDEDOR;
		}
		if ("GESTOR".equals(perfil) || "GESTOR_DE_STOCK".equals(perfil)) {
			return Usuario.Perfil.GESTOR_STOCK;
		}
		try {
			return Usuario.Perfil.valueOf(perfil);
		} catch (IllegalArgumentException e) {
			return Usuario.Perfil.VENDEDOR;
		}
	}

	private void copiarSeExistir(Path origem, Path destino) throws IOException {
		if (Files.exists(origem)) {
			Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void escreverLinhas(Path ficheiro, List<String> linhas) {
		try {
			Files.createDirectories(ficheiro.getParent());
			Files.write(ficheiro, linhas, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao escrever ficheiro: " + ficheiro, e);
		}
	}

	private String juntarCsv(String... valores) {
		List<String> campos = new ArrayList<>();
		for (String valor : valores) {
			campos.add(escaparCsv(valor));
		}
		return String.join(String.valueOf(DELIMITADOR_CSV_EXCEL), campos);
	}

	private String escaparCsv(String valor) {
		String texto = valor == null ? "" : valor;
		boolean precisaAspas = texto.indexOf(DELIMITADOR_CSV_EXCEL) >= 0
			|| texto.contains("\"")
			|| texto.contains("\n")
			|| texto.contains("\r");
		if (!precisaAspas) {
			return texto;
		}
		return "\"" + texto.replace("\"", "\"\"") + "\"";
	}

	private String[] separarCsv(String linha) {
		char delimitador = linha.indexOf(DELIMITADOR_CSV_EXCEL) >= 0 ? DELIMITADOR_CSV_EXCEL : ',';
		List<String> campos = new ArrayList<>();
		StringBuilder campo = new StringBuilder();
		boolean entreAspas = false;
		for (int i = 0; i < linha.length(); i++) {
			char atual = linha.charAt(i);
			if (atual == '"') {
				if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
					campo.append('"');
					i++;
				} else {
					entreAspas = !entreAspas;
				}
			} else if (atual == delimitador && !entreAspas) {
				campos.add(campo.toString());
				campo.setLength(0);
			} else {
				campo.append(atual);
			}
		}
		campos.add(campo.toString());
		return campos.toArray(new String[0]);
	}

	private String formatarNumeroCsv(double valor) {
		return FormatadorMoeda.numeroParaFicheiro(valor);
	}

	private double lerNumeroCsv(String valor) {
		return Double.parseDouble(valor.trim().replace(',', '.'));
	}

	private String nomeFicheiroProdutos(String idLoja) {
		return "loja_" + idLoja + "_produtos.txt";
	}

	private String nomeFicheiroVendas(String idLoja) {
		return "loja_" + idLoja + "_vendas.csv";
	}
}
