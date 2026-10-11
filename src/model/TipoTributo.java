package model;

/**
 * Pecas de tributo (35 no total: 10 espadas, 10 yurts e 15 moedas).
 */
enum TipoTributo {
	ESPADA(10),
	YURT(10),
	MOEDA(15);

	private final int quantidadeTotal;

	TipoTributo(int quantidadeTotal) {
		this.quantidadeTotal = quantidadeTotal;
	}

	int getQuantidadeTotal() {
		return quantidadeTotal;
	}

	static TipoTributo converter(String nome) {
		if (nome == null) {
			return null;
		}
		try {
			return TipoTributo.valueOf(nome.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
