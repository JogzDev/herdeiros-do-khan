package model;

/**
 * Cores dos jogadores.
 */
enum CorJogador {
	AZUL, AMARELO, CINZA, VERMELHO, VERDE;

	static CorJogador converter(String nome) {
		if (nome == null) {
			return null;
		}
		try {
			return CorJogador.valueOf(nome.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
