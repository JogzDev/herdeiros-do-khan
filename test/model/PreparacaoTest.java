package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * Preparacao da partida (manual, paginas 3 e 4) e habilidades iniciais.
 */
public class PreparacaoTest {
	private static final int TEMPO_LIMITE = 1000;

	/**
	 * Objetivo: o 1o e o 2o jogadores recebem 1 moeda; os demais, 2.
	 * Cenario: partida com 3 jogadores.
	 * Esperado: o 3o jogador comeca com 2 moedas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void terceiroJogadorRecebeDuasMoedas() {
		Jogo jogo = Cenario.comHerdeiros("CHAGATAI", "OGEDEI", "ALTANI");
		assertEquals("Do 3o jogador em diante sao 2 moedas", 2, jogo.getTributos("VERDE", "MOEDA"));
	}

	/**
	 * Objetivo: cada cidade revelada recebe 4 tesouros.
	 * Esperado: a primeira cidade disponivel tem 4 tesouros.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void cidadeReveladaTemQuatroTesouros() {
		Jogo jogo = Cenario.doisJogadores();
		String cidade = jogo.getCidadesReveladas().get(0);
		assertEquals("Manual: 4 pecas de tesouro por cidade", 4, jogo.getTesourosDaCidade(cidade).size());
	}

	/**
	 * Objetivo: Tolui comeca com 1 yurt em Karakorum.
	 * Esperado: Tolui tem 11 yurts disponiveis.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void toluiComecaComYurtEmKarakorum() {
		Jogo jogo = Cenario.comHerdeiros("TOLUI", "OGEDEI");
		assertEquals("1 dos 12 yurts ja esta em Karakorum", 11, jogo.getYurtsDisponiveis("AZUL"));
	}

	/**
	 * Objetivo: o yurt de Tolui em Karakorum nao conta para nenhuma regiao.
	 * Esperado: zero yurts azuis na Russia, China e Persia somadas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void yurtDeKarakorumNaoContaParaRegiao() {
		Jogo jogo = Cenario.comHerdeiros("TOLUI", "OGEDEI");
		int total = 0;
		for (Regiao r : Regiao.values()) {
			total += jogo.getMapa().yurtsNaRegiao(CorJogador.AZUL, r);
		}
		assertEquals("Karakorum nao pertence a nenhuma regiao", 0, total);
	}

	/**
	 * Objetivo: Altani tem 2 peoes, ambos em Karakorum no inicio.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniTemDoisPeoes() {
		Jogo jogo = Cenario.comHerdeiros("ALTANI", "OGEDEI");
		assertEquals("Altani joga com 2 peoes", 2, jogo.jogador("AZUL").getPosicoes().size());
	}

	/**
	 * Objetivo: sao reveladas 4 cartas de conselheiro aberto.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void quatroCartasAbertasReveladas() {
		Jogo jogo = Cenario.doisJogadores();
		assertEquals("Manual: 4 cartas de conselheiro aberto", 4, jogo.getCartasAbertas().size());
	}

	/**
	 * Objetivo: cada jogador recebe 2 cartas secretas e fica com 1.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void escolhaDoConselheiroSecreto() {
		Jogo jogo = new Jogo(Arrays.asList("AZUL", "VERMELHO"),
				Arrays.asList("CHAGATAI", "OGEDEI"), Cenario.SEMENTE);
		String segunda = jogo.getConselheirosSecretosOferecidos("AZUL").get(1);
		jogo.escolherConselheiroSecreto("AZUL", 1);
		assertTrue("A carta escolhida e a de indice 1",
				jogo.jogador("AZUL").getSecretaValida().name().equals(segunda));
	}

	/**
	 * Objetivo: cores repetidas sao rejeitadas na criacao da partida.
	 */
	@Test(timeout = TEMPO_LIMITE, expected = IllegalArgumentException.class)
	public void coresRepetidasSaoRejeitadas() {
		new Jogo(Arrays.asList("AZUL", "AZUL"), Arrays.asList("TOLUI", "OGEDEI"));
	}
}
