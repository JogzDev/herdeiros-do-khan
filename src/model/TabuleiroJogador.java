package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Tabuleiro de jogador: 3 colunas coloridas e a coluna branca, cada uma com 4
 * espacos de melhoria (linhas 0 a 3, de baixo para cima), e o controle das
 * pecas de ativacao (coluna usada ou disponivel).
 */
class TabuleiroJogador {
	static final int LINHAS = 4;

	private final Herdeiro herdeiro;
	private final HashMap<CorColuna, Melhoria[]> colunas = new HashMap<CorColuna, Melhoria[]>();
	private final HashMap<CorColuna, Boolean> usadas = new HashMap<CorColuna, Boolean>();

	TabuleiroJogador(Herdeiro herdeiro) {
		this.herdeiro = herdeiro;
		for (CorColuna c : CorColuna.values()) {
			colunas.put(c, new Melhoria[LINHAS]);
			usadas.put(c, Boolean.FALSE);
		}
	}

	// ------------------------------------------------------------ ativacao

	boolean estaDisponivel(CorColuna c) {
		return !usadas.get(c);
	}

	void marcarUsada(CorColuna c) {
		usadas.put(c, Boolean.TRUE);
	}

	void recuperarTodas() {
		for (CorColuna c : CorColuna.values()) {
			usadas.put(c, Boolean.FALSE);
		}
	}

	/** Acoes iniciais da coluna mais as das melhorias que ja podem ser usadas. */
	List<Acao> acoes(CorColuna c, int turnoAtual) {
		List<Acao> lista = new ArrayList<Acao>(herdeiro.getAcoesIniciais(c));
		for (Melhoria m : colunas.get(c)) {
			if (m != null && m.podeSerUsadaNoTurno(turnoAtual)) {
				lista.addAll(m.getTipo().acoes());
			}
		}
		return lista;
	}

	/** Melhorias de tesouro utilizaveis na coluna (cada uma da 1 tesouro virtual). */
	List<TipoTesouro> tesourosVirtuais(CorColuna c, int turnoAtual) {
		List<TipoTesouro> lista = new ArrayList<TipoTesouro>();
		for (Melhoria m : colunas.get(c)) {
			if (m != null && m.getTipo() == TipoMelhoria.TESOURO && m.podeSerUsadaNoTurno(turnoAtual)) {
				lista.add(m.getTesouro());
			}
		}
		return lista;
	}

	// ------------------------------------------------------------ melhorias

	int quantidade(CorColuna c) {
		int n = 0;
		for (Melhoria m : colunas.get(c)) {
			if (m != null) {
				n++;
			}
		}
		return n;
	}

	boolean podeColocar(CorColuna c) {
		return c.isColorida() && quantidade(c) < LINHAS;
	}

	/**
	 * Coloca a melhoria no proximo espaco livre da coluna colorida (de baixo
	 * para cima).
	 *
	 * @return a linha em que a melhoria foi colocada
	 */
	int colocar(CorColuna c, Melhoria m) {
		if (!podeColocar(c)) {
			throw new IllegalStateException("Coluna " + c + " nao aceita mais melhorias");
		}
		int linha = quantidade(c);
		colunas.get(c)[linha] = m;
		return linha;
	}

	boolean brancaOcupada(int linha) {
		return colunas.get(CorColuna.BRANCA)[linha] != null;
	}

	void colocarNaBranca(int linha, Melhoria m) {
		if (brancaOcupada(linha)) {
			throw new IllegalStateException("Espaco " + linha + " da coluna branca ja ocupado");
		}
		colunas.get(CorColuna.BRANCA)[linha] = m;
	}

	/** Uma linha esta completa quando as 3 colunas coloridas a preencheram. */
	boolean linhaCompleta(int linha) {
		for (CorColuna c : CorColuna.coloridas()) {
			if (colunas.get(c)[linha] == null) {
				return false;
			}
		}
		return true;
	}

	Melhoria getMelhoria(CorColuna c, int linha) {
		return colunas.get(c)[linha];
	}

	List<Melhoria> todasMelhorias() {
		List<Melhoria> lista = new ArrayList<Melhoria>();
		for (CorColuna c : CorColuna.values()) {
			for (Melhoria m : colunas.get(c)) {
				if (m != null) {
					lista.add(m);
				}
			}
		}
		return lista;
	}
}
