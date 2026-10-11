package model;

/**
 * Trilha do Kurultai. A ficha neutra avanca sempre que alguem ganha voto; quando
 * ela atinge o limite (10/14/16/17 para 2/3/4/5 jogadores) o fim do jogo e
 * disparado.
 */
class TrilhaKurultai {
	private static final int[] LIMITES = { 0, 0, 10, 14, 16, 17 };

	private final int limite;
	private int fichaNeutra;

	TrilhaKurultai(int numeroJogadores) {
		if (numeroJogadores < 2 || numeroJogadores > 5) {
			throw new IllegalArgumentException("Numero de jogadores invalido: " + numeroJogadores);
		}
		this.limite = LIMITES[numeroJogadores];
	}

	int getLimite() {
		return limite;
	}

	int getFichaNeutra() {
		return fichaNeutra;
	}

	void avancar(int votos) {
		fichaNeutra += votos;
	}

	boolean atingiuLimite() {
		return fichaNeutra >= limite;
	}
}
