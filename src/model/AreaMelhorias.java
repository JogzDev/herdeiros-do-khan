package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Area de melhorias do tabuleiro principal: 5 espacos e a pilha de pecas
 * viradas para baixo. Apos uma compra as pecas restantes deslizam para a
 * direita, deixando os espacos vazios a esquerda. Os espacos 1 e 3 tem os
 * icones de moeda e de espada (usados quando o Khan e colocado aqui).
 */
class AreaMelhorias {
	static final int TAMANHO = 5;
	static final int ESPACO_MOEDA = 1;
	static final int ESPACO_ESPADA = 3;

	private final Melhoria[] espacos = new Melhoria[TAMANHO];
	private final List<Melhoria> pilha;

	AreaMelhorias(List<Melhoria> pilhaEmbaralhada) {
		this.pilha = new ArrayList<Melhoria>(pilhaEmbaralhada);
		repor();
	}

	Melhoria getEspaco(int i) {
		return espacos[i];
	}

	boolean espacoVazio(int i) {
		return espacos[i] == null;
	}

	int tamanhoPilha() {
		return pilha.size();
	}

	/** Ha uma melhoria na pilha ou em algum dos espacos revelados. */
	boolean temMelhoriaDisponivel() {
		if (!pilha.isEmpty()) {
			return true;
		}
		for (Melhoria m : espacos) {
			if (m != null) {
				return true;
			}
		}
		return false;
	}

	Melhoria topoDaPilha() {
		return pilha.isEmpty() ? null : pilha.get(0);
	}

	/** Preenche os espacos vazios com pecas da pilha. */
	void repor() {
		for (int i = 0; i < TAMANHO; i++) {
			if (espacos[i] == null && !pilha.isEmpty()) {
				espacos[i] = pilha.remove(0);
			}
		}
	}

	/** Retira a melhoria do espaco e desliza as restantes para a direita. */
	Melhoria retirar(int i) {
		Melhoria m = espacos[i];
		if (m == null) {
			return null;
		}
		espacos[i] = null;
		deslizarParaDireita();
		return m;
	}

	Melhoria retirarDaPilha() {
		return pilha.isEmpty() ? null : pilha.remove(0);
	}

	/** Coloca uma melhoria num espaco especifico (usado na preparacao de testes). */
	void definirEspaco(int i, Melhoria m) {
		espacos[i] = m;
	}

	void devolverAPilha(Melhoria m) {
		pilha.add(m);
	}

	private void deslizarParaDireita() {
		int destino = TAMANHO - 1;
		for (int i = TAMANHO - 1; i >= 0; i--) {
			if (espacos[i] != null) {
				Melhoria m = espacos[i];
				espacos[i] = null;
				espacos[destino--] = m;
			}
		}
	}
}
