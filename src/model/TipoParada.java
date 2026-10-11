package model;

/**
 * Tipos de parada do mapa. Paradas duplas sao as paradas de conselheiro.
 */
enum TipoParada {
	SIMPLES(1, 1),
	DUPLA(2, 2),
	KARAKORUM(Integer.MAX_VALUE, 0);

	private final int capacidadePeoes;
	private final int capacidadeYurts;

	TipoParada(int capacidadePeoes, int capacidadeYurts) {
		this.capacidadePeoes = capacidadePeoes;
		this.capacidadeYurts = capacidadeYurts;
	}

	int getCapacidadePeoes() {
		return capacidadePeoes;
	}

	int getCapacidadeYurts() {
		return capacidadeYurts;
	}
}
