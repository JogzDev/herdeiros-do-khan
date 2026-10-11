package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Construcao de yurts (manual, pagina 11) e habilidade de Tolui.
 */
public class YurtTest {
	private static final int TEMPO_LIMITE = 1000;

	/**
	 * Objetivo: so se constroi yurt numa parada em que o peao esteve no turno.
	 * Cenario: AZUL/CHAGATAI em Karakorum, com 1 tributo de yurt.
	 * Esperado: nao pode construir em R1, onde nao esteve.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void naoConstroiEmParadaNaoVisitada() {
		Jogo jogo = Cenario.doisJogadores();
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.YURT, 1);
		assertFalse("O peao nao esteve em R1", jogo.construirYurt("R1"));
	}

	/**
	 * Objetivo: Karakorum nao aceita yurts construidos.
	 * Cenario: AZUL/CHAGATAI em Karakorum (parada visitada), com tributo de yurt.
	 * Esperado: a construcao e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void naoConstroiEmKarakorum() {
		Jogo jogo = Cenario.doisJogadores();
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.YURT, 1);
		assertFalse("Karakorum nao pode ter yurts", jogo.construirYurt(Mapa.KARAKORUM));
	}

	/**
	 * Objetivo: Tolui pode construir em parada adjacente a ele.
	 * Cenario: AZUL/TOLUI em Karakorum, com tributo de yurt; R1 e vizinha.
	 * Esperado: a construcao em R1 e aceita.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void toluiConstroiEmParadaAdjacente() {
		Jogo jogo = Cenario.comHerdeiros("TOLUI", "OGEDEI");
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.YURT, 1);
		assertTrue("Habilidade de Tolui", jogo.construirYurt("R1"));
	}

	/**
	 * Objetivo: parada simples comporta um unico yurt.
	 * Cenario: R1 ja tem yurt vermelho; o azul passou por R1.
	 * Esperado: a construcao e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void paradaSimplesComportaUmYurt() {
		Jogo jogo = Cenario.doisJogadores();
		Cenario.colocarYurt(jogo, "VERMELHO", "R1");
		Cenario.posicionarJogadorDaVez(jogo, "R1");
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.YURT, 1);
		assertFalse("R1 ja tem um yurt", jogo.construirYurt("R1"));
	}
}
