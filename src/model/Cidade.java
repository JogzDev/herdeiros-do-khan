package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Cidade do mapa. So pode ser atacada depois de revelada; ao perder o ultimo
 * tesouro e conquistada e recebe um yurt do conquistador.
 */
class Cidade {
	static final int TESOUROS_AO_REVELAR = 4;

	private final String nome;
	private final Regiao regiao;
	private final List<TipoTesouro> tesouros = new ArrayList<TipoTesouro>();
	private boolean revelada;
	private CorJogador conquistador;
	private CorJogador yurt;

	Cidade(String nome, Regiao regiao) {
		this.nome = nome;
		this.regiao = regiao;
	}

	String getNome() {
		return nome;
	}

	Regiao getRegiao() {
		return regiao;
	}

	List<TipoTesouro> getTesouros() {
		return tesouros;
	}

	boolean isRevelada() {
		return revelada;
	}

	void revelar() {
		revelada = true;
	}

	boolean isConquistada() {
		return conquistador != null;
	}

	boolean podeSerAtacada() {
		return revelada && !isConquistada() && !tesouros.isEmpty();
	}

	CorJogador getConquistador() {
		return conquistador;
	}

	CorJogador getYurt() {
		return yurt;
	}

	void conquistar(CorJogador cor, boolean colocaYurt) {
		conquistador = cor;
		if (colocaYurt) {
			yurt = cor;
		}
	}
}
