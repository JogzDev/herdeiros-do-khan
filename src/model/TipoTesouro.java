package model;

/**
 * Os 5 tipos de tesouro (8 pecas de cada). Cada tipo tem uma melhoria de
 * tesouro correspondente, com cor de fundo fixa.
 */
enum TipoTesouro {
	BOI(CorColuna.AMARELA),
	SEDA(CorColuna.VERDE),
	FERRO(CorColuna.VERMELHA),
	PELE(CorColuna.VERDE),
	ESPECIARIA(CorColuna.AMARELA);

	static final int PECAS_POR_TIPO = 8;

	private final CorColuna corMelhoria;

	TipoTesouro(CorColuna corMelhoria) {
		this.corMelhoria = corMelhoria;
	}

	CorColuna getCorMelhoria() {
		return corMelhoria;
	}

	static TipoTesouro converter(String nome) {
		if (nome == null) {
			return null;
		}
		try {
			return TipoTesouro.valueOf(nome.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
