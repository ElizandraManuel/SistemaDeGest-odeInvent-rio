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

	public boolean removerProduto(String idProduto) {
		if (idProduto == null) {
			return false;
		}
		return produtos.removeIf(produto -> produto.getIdProduto().equals(idProduto));
	}

	public Produto consultarProduto(String idProduto) {
		for (Produto produto : produtos) {
			if (produto.getIdProduto().equals(idProduto)) {
				return produto;
			}
		}
		return null;
	}

	public List<Produto> listarProdutos() {
		return Collections.unmodifiableList(produtos);
	}

	public void registarVenda(Venda venda) {
		if (venda == null) {
			throw new IllegalArgumentException("A venda não pode ser nula.");
		}
		vendas.add(venda);
	}

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