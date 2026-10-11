package model;

import java.util.List;

/**
 * Resultado da busca de caminho: o custo em movimentos e as paradas em que o
 * peao efetivamente esteve (paradas puladas sobre yurts proprios nao entram).
 */
class Caminho {
	private final int custo;
	private final List<String> paradasVisitadas;

	Caminho(int custo, List<String> paradasVisitadas) {
		this.custo = custo;
		this.paradasVisitadas = paradasVisitadas;
	}

	int getCusto() {
		return custo;
	}

	List<String> getParadasVisitadas() {
		return paradasVisitadas;
	}
}
