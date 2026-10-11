package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tipos de peca de melhoria (44 no total). A quantidade por tipo segue o
 * manual; as melhorias de tesouro sao 5, uma por tipo de tesouro.
 */
enum TipoMelhoria {
	CAVALOS(9),
	PUNHO(6),
	ESPADA(8),
	MOEDA(8),
	YURT(8),
	TESOURO(5);

	private final int quantidade;

	TipoMelhoria(int quantidade) {
		this.quantidade = quantidade;
	}

	int getQuantidade() {
		return quantidade;
	}

	/** Acoes que a melhoria acrescenta a coluna em que esta. */
	List<Acao> acoes() {
		List<Acao> lista = new ArrayList<Acao>();
		switch (this) {
		case CAVALOS:
			lista.add(Acao.CAVALO);
			lista.add(Acao.CAVALO);
			break;
		case PUNHO:
			lista.add(Acao.PUNHO);
			break;
		case ESPADA:
			lista.add(Acao.ESPADA_VIRTUAL);
			break;
		case MOEDA:
			lista.add(Acao.MOEDA_VIRTUAL);
			break;
		case YURT:
			lista.add(Acao.YURT_VIRTUAL);
			break;
		case TESOURO:
			lista.add(Acao.TESOURO_VIRTUAL);
			break;
		default:
			break;
		}
		return lista;
	}
}
