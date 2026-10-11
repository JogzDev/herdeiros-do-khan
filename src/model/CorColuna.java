package model;

/**
 * Colunas do tabuleiro de jogador. Cada coluna colorida esta associada a uma
 * regiao do mapa (usada na pontuacao de influencia do fim do jogo).
 */
enum CorColuna {
	VERMELHA("R", Regiao.CHINA),
	VERDE("V", Regiao.PERSIA),
	AMARELA("A", Regiao.RUSSIA),
	BRANCA("B", null);

	private final String sigla;
	private final Regiao regiao;

	CorColuna(String sigla, Regiao regiao) {
		this.sigla = sigla;
		this.regiao = regiao;
	}

	String getSigla() {
		return sigla;
	}

	Regiao getRegiao() {
		return regiao;
	}

	boolean isColorida() {
		return this != BRANCA;
	}

	static CorColuna[] coloridas() {
		return new CorColuna[] { VERMELHA, VERDE, AMARELA };
	}

	static CorColuna daRegiao(Regiao r) {
		for (CorColuna c : coloridas()) {
			if (c.regiao == r) {
				return c;
			}
		}
		return null;
	}

	static CorColuna converter(String nome) {
		if (nome == null) {
			return null;
		}
		try {
			return CorColuna.valueOf(nome.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
