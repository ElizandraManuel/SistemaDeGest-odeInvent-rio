package sistemadegestaodeinventario.modelo;

import sistemadegestaodeinventario.util.FormatadorMoeda;

public record ItemVenda(String idProduto, String nomeProduto, int quantidade, double precoUnitario, double subtotal) {

	public ItemVenda {
		if (idProduto == null || idProduto.trim().isEmpty()) {
			throw new IllegalArgumentException("O ID do produto não pode estar vazio.");
		}
		if (nomeProduto == null || nomeProduto.trim().isEmpty()) {
			throw new IllegalArgumentException("O nome do produto não pode estar vazio.");
		}
		if (quantidade <= 0) {
			throw new IllegalArgumentException("A quantidade deve ser superior a zero.");
		}
		if (precoUnitario < 0) {
			throw new IllegalArgumentException("O preço unitário não pode ser negativo.");
		}
	}

	public ItemVenda(String idProduto, String nomeProduto, int quantidade, double precoUnitario) {
		this(idProduto, nomeProduto, quantidade, precoUnitario, quantidade * precoUnitario);
	}

	public double calcularSubtotal() {
		return quantidade * precoUnitario;
	}

	@Override
	public String toString() {
		return String.format("%s | %s | qtde=%d | %s | subtotal=%s",
				idProduto, nomeProduto, quantidade, FormatadorMoeda.kz(precoUnitario), FormatadorMoeda.kz(subtotal));
	}
}
