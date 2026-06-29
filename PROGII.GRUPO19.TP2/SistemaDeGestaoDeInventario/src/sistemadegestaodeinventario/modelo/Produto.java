package sistemadegestaodeinventario.modelo;

import sistemadegestaodeinventario.util.FormatadorMoeda;

public class Produto {

	private String idProduto;
	private String nome;
	private String descricao;
	private double preco;
	private int quantidadeEmStock;
	private int quantidadeMinima;

	public Produto(String idProduto, String nome, String descricao, double preco, int quantidadeEmStock, int quantidadeMinima) {
		setIdProduto(idProduto);
		setNome(nome);
		setDescricao(descricao);
		setPreco(preco);
		setQuantidadeEmStock(quantidadeEmStock);
		setQuantidadeMinima(quantidadeMinima);
	}

	public String getIdProduto() {
		return idProduto;
	}

	public void setIdProduto(String idProduto) {
		if (idProduto == null || idProduto.trim().isEmpty()) {
			throw new IllegalArgumentException("O ID do produto não pode estar vazio.");
		}
		this.idProduto = idProduto.trim();
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		if (nome == null || nome.trim().isEmpty()) {
			throw new IllegalArgumentException("O nome do produto não pode estar vazio.");
		}
		this.nome = nome.trim();
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao == null ? "" : descricao.trim();
	}

	public double getPreco() {
		return preco;
	}

	public void setPreco(double preco) {
		if (preco < 0) {
			throw new IllegalArgumentException("O preço não pode ser negativo.");
		}
		this.preco = preco;
	}

	public int getQuantidadeEmStock() {
		return quantidadeEmStock;
	}

	public void setQuantidadeEmStock(int quantidadeEmStock) {
		if (quantidadeEmStock < 0) {
			throw new IllegalArgumentException("O stock não pode ser negativo.");
		}
		this.quantidadeEmStock = quantidadeEmStock;
	}

	public int getQuantidadeMinima() {
		return quantidadeMinima;
	}

	public void setQuantidadeMinima(int quantidadeMinima) {
		if (quantidadeMinima < 0) {
			throw new IllegalArgumentException("A quantidade mínima não pode ser negativa.");
		}
		this.quantidadeMinima = quantidadeMinima;
	}

	public void aumentarStock(int quantidade) {
		if (quantidade < 0) {
			throw new IllegalArgumentException("A quantidade a adicionar não pode ser negativa.");
		}
		quantidadeEmStock += quantidade;
	}

	public boolean diminuirStock(int quantidade) {
		if (quantidade < 0) {
			throw new IllegalArgumentException("A quantidade a remover não pode ser negativa.");
		}
		if (quantidadeEmStock < quantidade) {
			return false;
		}
		quantidadeEmStock -= quantidade;
		return true;
	}

	public int obterQuantidade() {
		return quantidadeEmStock;
	}

	public double obterPreco() {
		return preco;
	}

	public boolean estaAbaixoMinimo() {
		return quantidadeEmStock <= quantidadeMinima;
	}

	@Override
	public String toString() {
		return String.format("%s | %s | %s | %s | stock=%d | min=%d",
				idProduto, nome, descricao, FormatadorMoeda.kz(preco), quantidadeEmStock, quantidadeMinima);
	}
}
