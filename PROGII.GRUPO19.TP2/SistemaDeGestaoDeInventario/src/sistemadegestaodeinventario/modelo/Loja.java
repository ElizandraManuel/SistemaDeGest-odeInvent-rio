package sistemadegestaodeinventario.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Loja {

	private String idLoja;
	private String nome;
	private String endereco;
	private String telefone;
	private final List<Produto> produtos;
	private final List<Venda> vendas;

	public Loja(String idLoja, String nome, String endereco, String telefone) {
		setIdLoja(idLoja);
		setNome(nome);
		setEndereco(endereco);
		setTelefone(telefone);
		this.produtos = new ArrayList<>();
		this.vendas = new ArrayList<>();
	}

	public String getIdLoja() {
		return idLoja;
	}

	public void setIdLoja(String idLoja) {
		if (idLoja == null || idLoja.trim().isEmpty()) {
			throw new IllegalArgumentException("O ID da loja não pode estar vazio.");
		}
		this.idLoja = idLoja.trim();
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		if (nome == null || nome.trim().isEmpty()) {
			throw new IllegalArgumentException("O nome da loja não pode estar vazio.");
		}
		this.nome = nome.trim();
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco == null ? "" : endereco.trim();
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone == null ? "" : telefone.trim();
	}

        // GESTÃO DE STOCK: Adiciona um novo produto ou atualiza os dados e soma o stock se o produto já existir.
	public void adicionarProduto(Produto produto) {
		if (produto == null) {
			throw new IllegalArgumentException("O produto não pode ser nulo.");
		}
		Produto existente = consultarProduto(produto.getIdProduto());
		if (existente != null) {
			existente.setNome(produto.getNome());
			existente.setDescricao(produto.getDescricao());
			existente.setPreco(produto.getPreco());
			existente.setQuantidadeMinima(produto.getQuantidadeMinima());
			existente.aumentarStock(produto.getQuantidadeEmStock());
			return;
		}
		produtos.add(produto);
	}
        
// REMOÇÃO: Procura o produto pelo código e elimina-o do sistema.
	public void removerProduto(String idProduto) {
		produtos.removeIf(produto -> produto.getIdProduto().equals(idProduto));
	}

        // PROCURA: Percorre a lista de produtos e devolve o produto encontrado (ou "null" se não existir).
	public Produto consultarProduto(String idProduto) {
		for (Produto produto : produtos) {
			if (produto.getIdProduto().equals(idProduto)) {
				return produto;
			}
		}
		return null;
	}

        // CONSULTA DE LISTA: Devolve a lista de todos os produtos 
	public List<Produto> listarProdutos() {
		return Collections.unmodifiableList(produtos);
	}
        
// HISTÓRICO DE VENDAS: Recebe uma venda concluída e guarda-a no registo da loja.
	public void registarVenda(Venda venda) {
		if (venda == null) {
			throw new IllegalArgumentException("A venda não pode ser nula.");
		}
		vendas.add(venda);
	}

        // QUANTIDADE EM STOCK: Procura o produto e diz quantos restam (devolve -1 se o produto não existir).
	public int obterStock(String idProduto) {
		Produto produto = consultarProduto(idProduto);
		return produto == null ? -1 : produto.getQuantidadeEmStock();
	}

	public List<Venda> listarVendas() {
		return Collections.unmodifiableList(vendas);
	}

	public double obterTotalVendas() {
		return vendas.stream().mapToDouble(Venda::getTotalVenda).sum();
	}

	@Override
	public String toString() {
		return String.format("%s | %s | %s | %s | produtos=%d | vendas=%d",
				idLoja, nome, endereco, telefone, produtos.size(), vendas.size());
	}
}
