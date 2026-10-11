package model;

/**
 * Peca de melhoria: tipo (acao que acrescenta), cor de fundo e, nas melhorias
 * de tesouro, o tipo de tesouro virtual que ela fornece.
 */
class Melhoria {
	static final int NAO_ADQUIRIDA = Integer.MIN_VALUE;

	private final TipoMelhoria tipo;
	private final CorColuna cor;
	private final TipoTesouro tesouro;
	private int turnoAquisicao = NAO_ADQUIRIDA;

	Melhoria(TipoMelhoria tipo, CorColuna cor) {
		this(tipo, cor, null);
	}

	Melhoria(TipoMelhoria tipo, CorColuna cor, TipoTesouro tesouro) {
		if (tipo == TipoMelhoria.TESOURO && tesouro == null) {
			throw new IllegalArgumentException("Melhoria de tesouro precisa do tipo de tesouro");
		}
		this.tipo = tipo;
		this.cor = cor;
		this.tesouro = tesouro;
	}

	static Melhoria deTesouro(TipoTesouro t) {
		return new Melhoria(TipoMelhoria.TESOURO, t.getCorMelhoria(), t);
	}

	TipoMelhoria getTipo() {
		return tipo;
	}

	CorColuna getCor() {
		return cor;
	}

	TipoTesouro getTesouro() {
		return tesouro;
	}

	/** Simbolo usado nas cartas Colecionador e Sacerdote. */
	String getSimbolo() {
		return (tipo == TipoMelhoria.TESOURO) ? "TESOURO_" + tesouro.name() : tipo.name();
	}

	boolean temCavaloOuPunho() {
		return tipo == TipoMelhoria.CAVALOS || tipo == TipoMelhoria.PUNHO;
	}

	int getTurnoAquisicao() {
		return turnoAquisicao;
	}

	void setTurnoAquisicao(int turno) {
		this.turnoAquisicao = turno;
	}

	/** Uma melhoria nao pode ser usada no mesmo turno em que foi adquirida. */
	boolean podeSerUsadaNoTurno(int turno) {
		return turnoAquisicao < turno;
	}
}
