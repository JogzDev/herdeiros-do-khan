package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

/**
 * Pegar tributos e usar o Khan (manual, paginas 8 e 9).
 *
 * Jogador da vez: AZUL/CHAGATAI em Karakorum. A coluna VERMELHA de Chagatai
 * da 1 cavalo, 2 punhos e o Khan.
 */
public class TributoKhanTest {
	private static final int TEMPO_LIMITE = 1000;

	private Jogo jogo;

	@Before
	public void criarPartida() {
		jogo = Cenario.doisJogadores();
		jogo.ativarColuna("VERMELHA");
	}

	/**
	 * Objetivo: so se pega tributo de provincia adjacente a uma parada visitada.
	 * Cenario: o peao esta em Karakorum; PV3 fica na Persia, longe dele.
	 * Esperado: a acao e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void naoPegaTributoDeProvinciaDistante() {
		assertFalse("PV3 nao e adjacente a Karakorum", jogo.pegarTributo("PV3"));
	}

	/**
	 * Objetivo: pegar tributo de provincia adjacente transfere a peca.
	 * Cenario: KV (moeda) e adjacente a Karakorum. Chagatai comeca com 1 moeda.
	 * Esperado: o jogador fica com 2 moedas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void pegaTributoDeProvinciaAdjacente() {
		jogo.pegarTributo("KV");
		assertEquals("1 moeda inicial + 1 moeda tirada de KV", 2, jogo.getTributos("AZUL", "MOEDA"));
	}

	/**
	 * Objetivo: o Khan numa provincia abastece ela e as 2 apontadas pelas
	 * setas, respeitando o maximo de 3 pecas.
	 * Cenario: RV1 ja tem 3 pecas; o Khan vai para RV2 (setas para RV1 e RV3).
	 * Esperado: RV1 continua com 3 pecas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void khanRespeitaMaximoDeTresPecas() {
		Provincia rv1 = jogo.getMapa().getProvincia("RV1");
		while (!rv1.estaCheia()) {
			rv1.adicionarPeca();
		}
		jogo.moverKhan("RV2");
		assertEquals("RV1 ja estava cheia", 3, jogo.getPecasNaProvincia("RV1"));
	}

	/**
	 * Objetivo: o Khan abastece a provincia em que e colocado.
	 * Cenario: RV2 comeca com 1 peca.
	 * Esperado: RV2 passa a ter 2 pecas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void khanAbasteceProvinciaDestino() {
		jogo.moverKhan("RV2");
		assertEquals("1 peca inicial + 1 do Khan", 2, jogo.getPecasNaProvincia("RV2"));
	}

	/**
	 * Objetivo: o Khan nao pode permanecer onde esta.
	 * Cenario: azul coloca o Khan em RV2; no turno seguinte o vermelho
	 * (Ogedei, coluna AMARELA com Khan) tenta deixa-lo em RV2.
	 * Esperado: a acao e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void khanNaoPodePermanecerNoMesmoLugar() {
		jogo.moverKhan("RV2");
		jogo.finalizarTurno();
		jogo.ativarColuna("AMARELA");
		assertFalse("O Khan ja esta em RV2", jogo.moverKhan("RV2"));
	}

	/**
	 * Objetivo: o Khan na area de melhorias da moeda e espada quando os icones
	 * estao a mostra.
	 * Cenario: 4 melhorias retiradas da area (espacos 0 a 3 vazios).
	 * Esperado: o jogador ganha 1 espada (ele nao tinha nenhuma).
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void khanNaAreaDeMelhoriasDaEspadaComIconeAMostra() {
		for (int i = 0; i < 4; i++) {
			jogo.getAreaMelhorias().retirar(AreaMelhorias.TAMANHO - 1);
		}
		jogo.moverKhan(Jogo.AREA_MELHORIAS);
		assertEquals("O icone de espada estava a mostra", 1, jogo.getTributos("AZUL", "ESPADA"));
	}

	/**
	 * Objetivo: o turno nao termina enquanto o Khan (obrigatorio) nao for usado.
	 * Esperado: finalizarTurno e recusado.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void turnoNaoTerminaComKhanPendente() {
		assertFalse("A coluna vermelha exige mover o Khan", jogo.finalizarTurno());
	}
}
