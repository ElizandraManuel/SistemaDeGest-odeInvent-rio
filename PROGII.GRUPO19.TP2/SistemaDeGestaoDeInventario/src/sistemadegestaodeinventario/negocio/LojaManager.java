package sistemadegestaodeinventario.negocio;

import java.util.List;
import sistemadegestaodeinventario.modelo.Loja;
import sistemadegestaodeinventario.modelo.Produto;

public class LojaManager {

	private Loja lojaAtual;
	private final InventarioManager inventarioManager;

	public LojaManager(InventarioManager inventarioManager) {
		this.inventarioManager = inventarioManager;
	}

	public boolean selecionarLoja(String idLoja) {
		Loja loja = inventarioManager.obterLoja(idLoja);
		if (loja == null) {
			return false;
		}
		this.lojaAtual = loja;
		return true;
	}

	public Loja getLojaAtual() {
		return lojaAtual;
	}

	public void adicionarLoja(Loja loja) {
		inventarioManager.adicionarLoja(loja);
	}

	public void adicionarProduto(Produto produto) {
		if (lojaAtual == null) {
			throw new IllegalStateException("Nenhuma loja está selecionada.");
		}
		lojaAtual.adicionarProduto(produto);
	}

	public Produto obterProduto(String idProduto) {
		if (lojaAtual == null) {
			return null;
		}
		return lojaAtual.consultarProduto(idProduto);
	}

	public List<Produto> listarProdutos() {
		if (lojaAtual == null) {
			return List.of();
		}
		return lojaAtual.listarProdutos();
	}

	public String obterRelatorioLoja() {
		if (lojaAtual == null) {
			return "Nenhuma loja selecionada.";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Loja atual: ").append(lojaAtual).append('\n');
		sb.append("Produtos:\n");
		for (Produto produto : lojaAtual.listarProdutos()) {
			sb.append(" - ").append(produto).append('\n');
		}
		sb.append("Total de vendas: ").append(lojaAtual.listarVendas().size()).append('\n');
		sb.append("Valor vendido: ").append(String.format("%.2f", lojaAtual.obterTotalVendas())).append('\n');
		return sb.toString();
	}
}
