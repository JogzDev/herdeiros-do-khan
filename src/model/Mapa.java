package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Mapa do tabuleiro principal representado como um grafo: paradas ligadas por
 * rotas, com provincias, cidades e conselheiros associados as paradas.
 *
 * O mapa e montado por chamadas aos metodos adicionar*, o que separa os dados
 * da topologia (MapaProvisorio) das regras que operam sobre ela.
 */
class Mapa {
	static final String KARAKORUM = "KARAKORUM";

	private final HashMap<String, Parada> paradas = new HashMap<String, Parada>();
	private final HashMap<String, List<String>> rotas = new HashMap<String, List<String>>();
	private final HashMap<String, Provincia> provincias = new HashMap<String, Provincia>();
	private final HashMap<String, List<String>> provinciasDaParada = new HashMap<String, List<String>>();
	private final HashMap<String, List<String>> paradasDaProvincia = new HashMap<String, List<String>>();
	private final HashMap<String, Cidade> cidades = new HashMap<String, Cidade>();
	private final HashMap<String, List<String>> cidadesDaParada = new HashMap<String, List<String>>();
	private final HashMap<String, List<String>> paradasDaCidade = new HashMap<String, List<String>>();
	private final HashMap<String, Conselheiro> conselheiros = new HashMap<String, Conselheiro>();

	// ------------------------------------------------------------ construcao

	void adicionarParada(String id, TipoParada tipo, Regiao regiao) {
		if (paradas.containsKey(id)) {
			throw new IllegalArgumentException("Parada repetida: " + id);
		}
		paradas.put(id, new Parada(id, tipo, regiao));
		rotas.put(id, new ArrayList<String>());
		provinciasDaParada.put(id, new ArrayList<String>());
		cidadesDaParada.put(id, new ArrayList<String>());
	}

	void ligar(String a, String b) {
		exigirParada(a);
		exigirParada(b);
		if (!rotas.get(a).contains(b)) {
			rotas.get(a).add(b);
			rotas.get(b).add(a);
		}
	}

	void adicionarProvincia(String id, TipoTributo tipo, boolean doKhan, String... paradasAdjacentes) {
		provincias.put(id, new Provincia(id, tipo, doKhan));
		List<String> adj = new ArrayList<String>();
		for (String p : paradasAdjacentes) {
			exigirParada(p);
			adj.add(p);
			provinciasDaParada.get(p).add(id);
		}
		paradasDaProvincia.put(id, adj);
	}

	void definirSetasKhan(String provinciaKhan, String alvo1, String alvo2) {
		Provincia p = provincias.get(provinciaKhan);
		if (p == null || !p.isDoKhan() || !provincias.containsKey(alvo1) || !provincias.containsKey(alvo2)) {
			throw new IllegalArgumentException("Setas do Khan invalidas para " + provinciaKhan);
		}
		p.getSetasKhan().add(alvo1);
		p.getSetasKhan().add(alvo2);
	}

	void adicionarCidade(String nome, Regiao regiao, String... paradasAdjacentes) {
		cidades.put(nome, new Cidade(nome, regiao));
		List<String> adj = new ArrayList<String>();
		for (String p : paradasAdjacentes) {
			exigirParada(p);
			adj.add(p);
			cidadesDaParada.get(p).add(nome);
		}
		paradasDaCidade.put(nome, adj);
	}

	void adicionarConselheiro(Conselheiro c) {
		exigirParada(c.getParadaId());
		conselheiros.put(c.getParadaId(), c);
	}

	private void exigirParada(String id) {
		if (!paradas.containsKey(id)) {
			throw new IllegalArgumentException("Parada inexistente: " + id);
		}
	}

	// ------------------------------------------------------------ consultas

	Parada getParada(String id) {
		return paradas.get(id);
	}

	Collection<Parada> getParadas() {
		return paradas.values();
	}

	Provincia getProvincia(String id) {
		return provincias.get(id);
	}

	Collection<Provincia> getProvincias() {
		return provincias.values();
	}

	Cidade getCidade(String nome) {
		return cidades.get(nome);
	}

	Collection<Cidade> getCidades() {
		return cidades.values();
	}

	Conselheiro getConselheiro(String paradaId) {
		return conselheiros.get(paradaId);
	}

	Collection<Conselheiro> getConselheiros() {
		return conselheiros.values();
	}

	List<String> vizinhas(String paradaId) {
		List<String> v = rotas.get(paradaId);
		return v == null ? Collections.<String>emptyList() : v;
	}

	List<String> provinciasAdjacentes(String paradaId) {
		List<String> v = provinciasDaParada.get(paradaId);
		return v == null ? Collections.<String>emptyList() : v;
	}

	List<String> paradasAdjacentesAProvincia(String provinciaId) {
		List<String> v = paradasDaProvincia.get(provinciaId);
		return v == null ? Collections.<String>emptyList() : v;
	}

	List<String> cidadesAdjacentes(String paradaId) {
		List<String> v = cidadesDaParada.get(paradaId);
		return v == null ? Collections.<String>emptyList() : v;
	}

	List<String> paradasAdjacentesACidade(String nome) {
		List<String> v = paradasDaCidade.get(nome);
		return v == null ? Collections.<String>emptyList() : v;
	}

	// ------------------------------------------------------------ algoritmos

	/**
	 * Caminho mais barato entre duas paradas. Entrar numa parada custa 1
	 * movimento, exceto quando ela tem yurt proprio e o peao apenas passa por
	 * ela (pula a parada). A parada de destino sempre custa 1.
	 *
	 * @return o caminho, ou null se o destino for inalcancavel
	 */
	Caminho caminhoMaisBarato(String origem, String destino, Set<String> pulaveis) {
		if (!paradas.containsKey(origem) || !paradas.containsKey(destino) || origem.equals(destino)) {
			return null;
		}
		HashMap<String, Integer> distancia = new HashMap<String, Integer>();
		HashMap<String, String> anterior = new HashMap<String, String>();
		HashSet<String> fechados = new HashSet<String>();
		distancia.put(origem, 0);

		while (true) {
			String atual = null;
			for (String id : distancia.keySet()) {
				if (!fechados.contains(id) && (atual == null || distancia.get(id) < distancia.get(atual))) {
					atual = id;
				}
			}
			if (atual == null) {
				return null;
			}
			if (atual.equals(destino)) {
				break;
			}
			fechados.add(atual);
			for (String viz : vizinhas(atual)) {
				if (fechados.contains(viz)) {
					continue;
				}
				int custoEntrada = (!viz.equals(destino) && pulaveis.contains(viz)) ? 0 : 1;
				int nova = distancia.get(atual) + custoEntrada;
				Integer antiga = distancia.get(viz);
				if (antiga == null || nova < antiga) {
					distancia.put(viz, nova);
					anterior.put(viz, atual);
				}
			}
		}

		LinkedList<String> visitadas = new LinkedList<String>();
		String passo = destino;
		while (!passo.equals(origem)) {
			boolean pulada = !passo.equals(destino) && pulaveis.contains(passo);
			if (!pulada) {
				visitadas.addFirst(passo);
			}
			passo = anterior.get(passo);
		}
		return new Caminho(distancia.get(destino), visitadas);
	}

	/** Numero de rotas entre duas paradas (busca em largura). */
	int distancia(String origem, String destino) {
		if (origem.equals(destino)) {
			return 0;
		}
		HashMap<String, Integer> dist = new HashMap<String, Integer>();
		LinkedList<String> fila = new LinkedList<String>();
		dist.put(origem, 0);
		fila.add(origem);
		while (!fila.isEmpty()) {
			String atual = fila.removeFirst();
			for (String viz : vizinhas(atual)) {
				if (!dist.containsKey(viz)) {
					dist.put(viz, dist.get(atual) + 1);
					if (viz.equals(destino)) {
						return dist.get(viz);
					}
					fila.add(viz);
				}
			}
		}
		return Integer.MAX_VALUE;
	}

	/** Yurts de um jogador numa regiao: paradas da regiao e cidades conquistadas. */
	int yurtsNaRegiao(CorJogador cor, Regiao regiao) {
		int total = 0;
		for (Parada p : paradas.values()) {
			if (p.getRegiao() == regiao) {
				total += p.quantidadeYurtsDe(cor);
			}
		}
		for (Cidade c : cidades.values()) {
			if (c.getRegiao() == regiao && c.getYurt() == cor) {
				total++;
			}
		}
		return total;
	}

	/** Quantidade de yurts do jogador nas paradas adjacentes a uma provincia. */
	int yurtsAdjacentesAProvincia(CorJogador cor, String provinciaId) {
		int total = 0;
		for (String p : paradasAdjacentesAProvincia(provinciaId)) {
			total += paradas.get(p).quantidadeYurtsDe(cor);
		}
		return total;
	}

	/** Quantidade de yurts do jogador nas paradas adjacentes a uma cidade. */
	int yurtsAdjacentesACidade(CorJogador cor, String nomeCidade) {
		int total = 0;
		for (String p : paradasAdjacentesACidade(nomeCidade)) {
			total += paradas.get(p).quantidadeYurtsDe(cor);
		}
		return total;
	}

	/** Tamanho da maior rede de yurts conectados por rotas (carta Viajante). */
	int maiorRedeDeYurts(CorJogador cor) {
		HashSet<String> vistos = new HashSet<String>();
		int maior = 0;
		for (Parada inicio : paradas.values()) {
			if (!inicio.temYurtDe(cor) || vistos.contains(inicio.getId())) {
				continue;
			}
			int tamanho = 0;
			LinkedList<String> fila = new LinkedList<String>();
			fila.add(inicio.getId());
			vistos.add(inicio.getId());
			while (!fila.isEmpty()) {
				String atual = fila.removeFirst();
				tamanho += paradas.get(atual).quantidadeYurtsDe(cor);
				for (String viz : vizinhas(atual)) {
					if (!vistos.contains(viz) && paradas.get(viz).temYurtDe(cor)) {
						vistos.add(viz);
						fila.add(viz);
					}
				}
			}
			maior = Math.max(maior, tamanho);
		}
		return maior;
	}
}
