package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Parada do mapa (ponto onde um peao pode estar). Guarda os yurts construidos
 * nela, identificados pela cor do dono.
 */
class Parada {
	private final String id;
	private final TipoParada tipo;
	private final Regiao regiao;
	private final List<CorJogador> yurts = new ArrayList<CorJogador>();

	Parada(String id, TipoParada tipo, Regiao regiao) {
		this.id = id;
		this.tipo = tipo;
		this.regiao = regiao;
	}

	String getId() {
		return id;
	}

	TipoParada getTipo() {
		return tipo;
	}

	Regiao getRegiao() {
		return regiao;
	}

	List<CorJogador> getYurts() {
		return yurts;
	}

	boolean temYurtDe(CorJogador cor) {
		return yurts.contains(cor);
	}

	int quantidadeYurtsDe(CorJogador cor) {
		int n = 0;
		for (CorJogador c : yurts) {
			if (c == cor) {
				n++;
			}
		}
		return n;
	}

	boolean cabeYurt() {
		return yurts.size() < tipo.getCapacidadeYurts();
	}

	void adicionarYurt(CorJogador cor) {
		yurts.add(cor);
	}
}
