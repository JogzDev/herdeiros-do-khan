package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Regras de movimento (manual, pagina 7).
 *
 * Em todos os casos o jogador da vez e AZUL/CHAGATAI, comecando em Karakorum.
 * A coluna AMARELA de Chagatai da 2 movimentos.
 */
public class MovimentoTest {
	private static final int TEMPO_LIMITE = 1000;

	private Jogo jogo;

	@Before
	public void criarPartida() {
		jogo = Cenario.doisJogadores();
	}

	/**
	 * Objetivo: sem yurts no caminho, cada parada custa 1 movimento.
	 * Cenario: 2 movimentos; R3 esta a 3 paradas de Karakorum (R1, R2, R3).
	 * Esperado: o movimento e recusado.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void naoAlcancaParadaMaisDistanteQueOsMovimentos() {
		jogo.ativarColuna("AMARELA");
		assertFalse("R3 exige 3 movimentos e o jogador so tem 2", jogo.podeMoverPara("R3"));
	}

	/**
	 * Objetivo: pular um yurt proprio nao gasta movimento.
	 * Cenario: mesmo caso anterior, mas com yurt azul em R1.
	 * Esperado: chega em R3 gastando os 2 movimentos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void pularYurtProprioNaoGastaMovimento() {
		Cenario.colocarYurt(jogo, "AZUL", "R1");
		jogo.ativarColuna("AMARELA");
		jogo.moverPara("R3");
		assertEquals("R1 foi pulada, entao R2 e R3 custam os 2 movimentos", 0, jogo.getMovimentosRestantes());
	}

	/**
	 * Objetivo: nao se pode terminar o movimento numa parada simples ocupada.
	 * Cenario: o peao vermelho esta em R1.
	 * Esperado: o azul nao pode parar em R1.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void naoTerminaEmParadaSimplesOcupada() {
		jogo.jogador("VERMELHO").setPosicao("R1");
		jogo.ativarColuna("AMARELA");
		assertFalse("R1 ja tem um peao e so cabe um", jogo.podeMoverPara("R1"));
	}

	/**
	 * Objetivo: pode-se passar por uma parada ocupada.
	 * Cenario: o peao vermelho esta em R1; o azul vai para R2 passando por R1.
	 * Esperado: o movimento e permitido.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void passaPorParadaOcupada() {
		jogo.jogador("VERMELHO").setPosicao("R1");
		jogo.ativarColuna("AMARELA");
		assertTrue("Passar por R1 ocupada e permitido", jogo.moverPara("R2"));
	}

	/**
	 * Objetivo: parada dupla aceita um segundo peao.
	 * Cenario: o peao vermelho esta em RD1 (parada dupla, a 2 paradas de Karakorum).
	 * Esperado: o azul pode terminar o movimento em RD1.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void terminaEmParadaDuplaComUmOcupante() {
		jogo.jogador("VERMELHO").setPosicao("RD1");
		jogo.ativarColuna("AMARELA");
		assertTrue("Na parada dupla cabem 2 peoes", jogo.moverPara("RD1"));
	}

	/**
	 * Objetivo: a parada pulada nao conta como visitada para pegar tributo.
	 * Cenario: mapa minimo KARAKORUM-B-C, provincia X adjacente so a B, yurt
	 * azul em B; o azul vai de Karakorum a C pulando B.
	 * Esperado: nao pode pegar tributo de X.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void paradaPuladaNaoPermitePegarTributo() {
		Jogo pequeno = new Jogo(Arrays.asList("AZUL", "VERMELHO"), Arrays.asList("CHAGATAI", "OGEDEI"),
				MapaTeste.criar(), new Random(Cenario.SEMENTE));
		Cenario.prepararEscolhas(pequeno, "AZUL", "VERMELHO");
		Cenario.colocarYurt(pequeno, "AZUL", "B");
		pequeno.ativarColuna("AMARELA");
		pequeno.moverPara("C");
		assertFalse("B foi pulada, entao a provincia X nao esta ao alcance", pequeno.pegarTributo("X"));
	}

	/**
	 * Objetivo: sem coluna ativada o jogador nao tem cavalos.
	 * Esperado: nenhum movimento e possivel.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void naoMoveSemAtivarColuna() {
		assertFalse("Sem coluna ativada nao ha movimentos", jogo.podeMoverPara("R1"));
	}
}
