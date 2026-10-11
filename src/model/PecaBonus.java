package model;

/**
 * As 7 pecas de bonus. Sao colocadas aleatoriamente nas 7 paradas de
 * conselheiro e vao para o primeiro jogador que fizer uma entrega ali.
 */
enum PecaBonus {
	VOTO,
	DUAS_ESPADAS,
	DUAS_MOEDAS,
	DOIS_YURTS,
	TRES_CAVALOS_UM_PUNHO,
	DOIS_PUNHOS,
	TRES_TESOUROS;

	/** O bonus de voto e aplicado imediatamente; os demais sao guardados. */
	boolean isImediato() {
		return this == VOTO;
	}

	static PecaBonus converter(String nome) {
		if (nome == null) {
			return null;
		}
		try {
			return PecaBonus.valueOf(nome.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
