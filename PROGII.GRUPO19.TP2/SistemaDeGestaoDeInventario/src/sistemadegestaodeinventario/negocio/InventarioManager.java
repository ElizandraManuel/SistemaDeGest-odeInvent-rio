package sistemadegestaodeinventario.negocio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sistemadegestaodeinventario.modelo.ItemVenda;
import sistemadegestaodeinventario.modelo.Loja;
import sistemadegestaodeinventario.modelo.Produto;
import sistemadegestaodeinventario.modelo.Venda;
import sistemadegestaodeinventario.util.FormatadorMoeda;

public class InventarioManager {

	private final Map<String, Loja> lojas;

	public InventarioManager() {
		this.lojas = new HashMap<>();
	}

	public void adicionarLoja(Loja loja) {
		if (loja == null) {
			throw new IllegalArgumentException("A loja não pode ser nula.");
		}
		lojas.put(loja.getIdLoja(), loja);
	}

	public Loja obterLoja(String idLoja) {
		return idLoja == null ? null : lojas.get(idLoja);
	}

	public List<Loja> listarLojas() {
		return new ArrayList<>(lojas.values());
	}

	public boolean existeLoja(String idLoja) {
		return lojas.containsKey(idLoja);
	}

	public Venda registarVenda(String idLoja, List<ItemVenda> itens) {
		return registarVenda(idLoja, itens, "");
	}

	public Venda registarVenda(String idLoja, List<ItemVenda> itens, String idVendedor) {
		Loja loja = obterLoja(idLoja);
		if (loja == null) {
			throw new IllegalArgumentException("Loja não encontrada.");
		}
		if (itens == null || itens.isEmpty()) {
			throw new IllegalArgumentException("A venda deve conter pelo menos um item.");
		}

		for (ItemVenda item : itens) {
			Produto produto = loja.consultarProduto(item.idProduto());
			if (produto == null) {
				throw new IllegalArgumentException("Produto não encontrado: " + item.idProduto());
			}
			if (produto.getQuantidadeEmStock() < item.quantidade()) {
				throw new IllegalArgumentException("Stock insuficiente para o produto: " + produto.getNome());
			}
		}

		String idVenda = gerarIdVenda(idLoja);
		Venda venda = new Venda(idVenda, LocalDateTime.now(), idLoja, idVendedor);
		for (ItemVenda item : itens) {
			Produto produto = loja.consultarProduto(item.idProduto());
			produto.diminuirStock(item.quantidade());
			venda.adicionarItem(new ItemVenda(produto.getIdProduto(), produto.getNome(), item.quantidade(), produto.getPreco()));
		}
		venda.calcularTotal();
		loja.registarVenda(venda);
		return venda;
	}

	public int consultarStockProduto(String idLoja, String idProduto) {
		Loja loja = obterLoja(idLoja);
		if (loja == null) {
			return -1;
		}
		return loja.obterStock(idProduto);
	}

	public List<Produto> gerirStockBaixo() {
		List<Produto> produtosBaixos = new ArrayList<>();
		for (Loja loja : lojas.values()) {
			for (Produto produto : loja.listarProdutos()) {
				if (produto.estaAbaixoMinimo()) {
					produtosBaixos.add(produto);
				}
			}
		}
		return produtosBaixos;
	}

	public String obterRelatorioVendas(String idLoja) {
		Loja loja = obterLoja(idLoja);
		if (loja == null) {
			return "Loja não encontrada.";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Relatório de vendas da loja ").append(loja.getNome()).append('\n');
		sb.append("Total de vendas: ").append(loja.listarVendas().size()).append('\n');
		sb.append("Valor total: ").append(FormatadorMoeda.kz(loja.obterTotalVendas())).append('\n');
		for (Venda venda : loja.listarVendas()) {
			sb.append(venda).append('\n');
		}
		return sb.toString();
	}

	public String obterRelatorioSistema() {
		int totalProdutos = 0;
		double totalVendas = 0.0;
		for (Loja loja : lojas.values()) {
			totalProdutos += loja.listarProdutos().size();
			totalVendas += loja.obterTotalVendas();
		}
		return "Lojas: " + lojas.size()
				+ "\nProdutos: " + totalProdutos
				+ "\nVendas: " + FormatadorMoeda.kz(totalVendas);
	}

	public List<Loja> getLojasOrdenadas() {
		List<Loja> lista = new ArrayList<>(lojas.values());
		lista.sort((a, b) -> a.getIdLoja().compareToIgnoreCase(b.getIdLoja()));
		return lista;
	}

	public boolean deletarLoja(String idLoja) {
		if (idLoja == null) {
			return false;
		}
		return lojas.remove(idLoja) != null;
	}

	private String gerarIdVenda(String idLoja) {
		return "V-" + idLoja + "-" + System.currentTimeMillis();
	}
}
