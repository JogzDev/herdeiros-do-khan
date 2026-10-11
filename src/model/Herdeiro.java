package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Os 5 herdeiros. Cada um tem as acoes iniciais de suas colunas (impressas
 * na linha de baixo do tabuleiro) e uma habilidade especial.
 */
enum Herdeiro {
	ALTANI(2,
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.PUNHO, Acao.KHAN },
			new Acao[] { Acao.CAVALO, Acao.CAVALO, Acao.PUNHO },
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.MOEDA_VIRTUAL }),
	JOCHI(1,
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.MOEDA_VIRTUAL },
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.PUNHO, Acao.KHAN },
			new Acao[] { Acao.CAVALO, Acao.CAVALO, Acao.PUNHO }),
	CHAGATAI(1,
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.PUNHO, Acao.KHAN },
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.MOEDA_VIRTUAL },
			new Acao[] { Acao.CAVALO, Acao.CAVALO, Acao.PUNHO }),
	TOLUI(1,
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.MOEDA_VIRTUAL },
			new Acao[] { Acao.CAVALO, Acao.CAVALO, Acao.PUNHO },
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.PUNHO, Acao.KHAN }),
	OGEDEI(1,
			new Acao[] { Acao.CAVALO, Acao.CAVALO, Acao.PUNHO },
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.MOEDA_VIRTUAL },
			new Acao[] { Acao.CAVALO, Acao.PUNHO, Acao.PUNHO, Acao.KHAN });

	private final int numeroPeoes;
	private final HashMap<CorColuna, List<Acao>> acoesIniciais = new HashMap<CorColuna, List<Acao>>();

	Herdeiro(int numeroPeoes, Acao[] vermelha, Acao[] verde, Acao[] amarela) {
		this.numeroPeoes = numeroPeoes;
		acoesIniciais.put(CorColuna.VERMELHA, comoLista(vermelha));
		acoesIniciais.put(CorColuna.VERDE, comoLista(verde));
		acoesIniciais.put(CorColuna.AMARELA, comoLista(amarela));
		acoesIniciais.put(CorColuna.BRANCA, comoLista(new Acao[] { Acao.CAVALO, Acao.CAVALO, Acao.KHAN, Acao.RECUPERAR }));
	}

	private static List<Acao> comoLista(Acao[] acoes) {
		List<Acao> lista = new ArrayList<Acao>();
		Collections.addAll(lista, acoes);
		return Collections.unmodifiableList(lista);
	}

	int getNumeroPeoes() {
		return numeroPeoes;
	}

	List<Acao> getAcoesIniciais(CorColuna coluna) {
		return acoesIniciais.get(coluna);
	}

	static Herdeiro converter(String nome) {
		if (nome == null) {
			return null;
		}
		try {
			return Herdeiro.valueOf(nome.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
