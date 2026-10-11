package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Estado de um jogador: herdeiro, posicao do(s) peao(s), tributos, tesouros,
 * yurts, cartas de conselheiro secreto, bonus e votos.
 */
class Jogador {
	static final int YURTS_INICIAIS = 12;

	private final CorJogador cor;
	private final Herdeiro herdeiro;
	private final TabuleiroJogador tabuleiro;
	private final List<String> posicoes = new ArrayList<String>();
	private int peaoAtivo;
	private final HashMap<TipoTributo, Integer> tributos = new HashMap<TipoTributo, Integer>();
	private final HashMap<TipoTesouro, Integer> tesouros = new HashMap<TipoTesouro, Integer>();
	private int yurtsDisponiveis = YURTS_INICIAIS;
	private final List<String> cidadesConquistadas = new ArrayList<String>();
	private final List<CartaConselheiroSecreto> secretasOferecidas = new ArrayList<CartaConselheiroSecreto>();
	private CartaConselheiroSecreto secretaEscolhida;
	private final List<PecaBonus> bonusDisponiveis = new ArrayList<PecaBonus>();
	private int bonusObtidos;
	private int votos;
	private final List<Melhoria> melhoriasIniciaisOferecidas = new ArrayList<Melhoria>();

	Jogador(CorJogador cor, Herdeiro herdeiro, String posicaoInicial) {
		this.cor = cor;
		this.herdeiro = herdeiro;
		this.tabuleiro = new TabuleiroJogador(herdeiro);
		for (int i = 0; i < herdeiro.getNumeroPeoes(); i++) {
			posicoes.add(posicaoInicial);
		}
		for (TipoTributo t : TipoTributo.values()) {
			tributos.put(t, 0);
		}
		for (TipoTesouro t : TipoTesouro.values()) {
			tesouros.put(t, 0);
		}
	}

	CorJogador getCor() {
		return cor;
	}

	Herdeiro getHerdeiro() {
		return herdeiro;
	}

	TabuleiroJogador getTabuleiro() {
		return tabuleiro;
	}

	// ------------------------------------------------------------ peoes

	String getPosicao() {
		return posicoes.get(peaoAtivo);
	}

	List<String> getPosicoes() {
		return posicoes;
	}

	void setPosicao(String parada) {
		posicoes.set(peaoAtivo, parada);
	}

	int getPeaoAtivo() {
		return peaoAtivo;
	}

	void setPeaoAtivo(int indice) {
		if (indice < 0 || indice >= posicoes.size()) {
			throw new IllegalArgumentException("Peao inexistente: " + indice);
		}
		peaoAtivo = indice;
	}

	// ------------------------------------------------------------ tributos

	int getTributos(TipoTributo t) {
		return tributos.get(t);
	}

	void adicionarTributo(TipoTributo t, int n) {
		tributos.put(t, tributos.get(t) + n);
	}

	void removerTributo(TipoTributo t, int n) {
		if (tributos.get(t) < n) {
			throw new IllegalStateException("Tributos insuficientes de " + t);
		}
		tributos.put(t, tributos.get(t) - n);
	}

	// ------------------------------------------------------------ tesouros

	int getTesouros(TipoTesouro t) {
		return tesouros.get(t);
	}

	int totalTesouros() {
		int total = 0;
		for (int n : tesouros.values()) {
			total += n;
		}
		return total;
	}

	void adicionarTesouro(TipoTesouro t) {
		tesouros.put(t, tesouros.get(t) + 1);
	}

	void removerTesouro(TipoTesouro t) {
		if (tesouros.get(t) == 0) {
			throw new IllegalStateException("Jogador nao tem " + t);
		}
		tesouros.put(t, tesouros.get(t) - 1);
	}

	// ------------------------------------------------------------ yurts

	int getYurtsDisponiveis() {
		return yurtsDisponiveis;
	}

	int getYurtsNoMapa() {
		return YURTS_INICIAIS - yurtsDisponiveis;
	}

	void usarYurt() {
		if (yurtsDisponiveis == 0) {
			throw new IllegalStateException("Sem yurts disponiveis");
		}
		yurtsDisponiveis--;
	}

	List<String> getCidadesConquistadas() {
		return cidadesConquistadas;
	}

	// ------------------------------------------------------------ cartas

	List<CartaConselheiroSecreto> getSecretasOferecidas() {
		return secretasOferecidas;
	}

	CartaConselheiroSecreto getSecretaEscolhida() {
		return secretaEscolhida;
	}

	void setSecretaEscolhida(CartaConselheiroSecreto carta) {
		this.secretaEscolhida = carta;
	}

	/** Carta explicitamente escolhida na preparacao, ou null antes da escolha. */
	CartaConselheiroSecreto getSecretaValida() {
		return secretaEscolhida;
	}

	List<Melhoria> getMelhoriasIniciaisOferecidas() {
		return melhoriasIniciaisOferecidas;
	}

	// ------------------------------------------------------------ bonus

	List<PecaBonus> getBonusDisponiveis() {
		return bonusDisponiveis;
	}

	int getBonusObtidos() {
		return bonusObtidos;
	}

	void receberBonus(PecaBonus b) {
		bonusObtidos++;
		if (!b.isImediato()) {
			bonusDisponiveis.add(b);
		}
	}

	// ------------------------------------------------------------ votos

	int getVotos() {
		return votos;
	}

	void adicionarVotos(int n) {
		votos += n;
	}
}
