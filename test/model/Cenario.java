package model;

import java.util.Arrays;
import java.util.Random;

/**
 * Metodos auxiliares compartilhados pelos casos de teste. Cada teste cria o
 * seu proprio jogo, entao nenhum estado mutavel e compartilhado entre testes.
 */
final class Cenario {
	static final long SEMENTE = 42L;

	private Cenario() {
	}

	/** Partida com 2 jogadores: AZUL/CHAGATAI e VERMELHO/OGEDEI. */
	static Jogo doisJogadores() {
		Jogo jogo = new Jogo(Arrays.asList("AZUL", "VERMELHO"), Arrays.asList("CHAGATAI", "OGEDEI"),
				MapaProvisorio.criar(), new Random(SEMENTE));
		prepararEscolhas(jogo, "AZUL", "VERMELHO");
		return jogo;
	}

	static Jogo comHerdeiros(String... herdeiros) {
		String[] cores = { "AZUL", "VERMELHO", "VERDE", "AMARELO", "CINZA" };
		Jogo jogo = new Jogo(Arrays.asList(Arrays.copyOf(cores, herdeiros.length)), Arrays.asList(herdeiros),
				MapaProvisorio.criar(), new Random(SEMENTE));
		prepararEscolhas(jogo, Arrays.copyOf(cores, herdeiros.length));
		return jogo;
	}

	/** Resolve escolhas iniciais para testes que precisam de uma partida pronta. */
	static void prepararEscolhas(Jogo jogo, String... cores) {
		for (String cor : cores) {
			if (!jogo.escolherConselheiroSecreto(cor, 0)) {
				throw new IllegalStateException("Nao foi possivel escolher a carta de " + cor);
			}
			if (!jogo.getMelhoriasIniciaisOferecidas(cor).isEmpty()
					&& !jogo.escolherMelhoriaInicial(cor, 0)) {
				throw new IllegalStateException("Nao foi possivel escolher a melhoria de " + cor);
			}
		}
	}

	/** Coloca o peao do jogador da vez numa parada, como se tivesse comecado o turno nela. */
	static void posicionarJogadorDaVez(Jogo jogo, String parada) {
		jogo.jogadorDaVez().setPosicao(parada);
		jogo.getTurno().reiniciarParadasVisitadas(parada);
	}

	/** Constroi um yurt sem passar pelas regras (preparacao de cenario). */
	static void colocarYurt(Jogo jogo, String cor, String parada) {
		Jogador j = jogo.jogador(cor);
		j.usarYurt();
		jogo.getMapa().getParada(parada).adicionarYurt(j.getCor());
	}

	/**
	 * Joga um turno sem acoes: ativa a primeira coluna disponivel e, se ela
	 * tiver o Khan, move-o para uma provincia do Khan diferente da atual.
	 */
	static void jogarTurnoVazio(Jogo jogo) {
		String[] colunas = { "AMARELA", "VERDE", "VERMELHA", "BRANCA" };
		for (String c : colunas) {
			if (jogo.ativarColuna(c)) {
				break;
			}
		}
		if (jogo.isKhanPendente()) {
			String[] destinos = { "RV2", "CV2" };
			for (String d : destinos) {
				if (jogo.moverKhan(d)) {
					break;
				}
			}
		}
		if (!jogo.finalizarTurno()) {
			throw new IllegalStateException("Turno vazio nao pode ser finalizado");
		}
	}
}
