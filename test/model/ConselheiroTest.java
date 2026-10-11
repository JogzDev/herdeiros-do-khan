package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Entregas aos conselheiros, pecas de bonus e cartas de conselheiro aberto
 * (manual, paginas 12, 13, 17 e 18).
 *
 * Jogador da vez: AZUL/CHAGATAI em Karakorum. O conselheiro de Karakorum pede
 * SEDA, FERRO e ESPECIARIA e da 2 votos.
 */
public class ConselheiroTest {
	private static final int TEMPO_LIMITE = 1000;

	private Jogo jogo;
	private Jogador azul;

	@Before
	public void criarPartida() {
		jogo = Cenario.doisJogadores();
		azul = jogo.jogador("AZUL");
		azul.adicionarTesouro(TipoTesouro.SEDA);
		azul.adicionarTesouro(TipoTesouro.FERRO);
		azul.adicionarTesouro(TipoTesouro.ESPECIARIA);
		// sem cartas abertas, para que nenhum voto extra interfira nos testes
		jogo.getCartasAbertasInternas().clear();
	}

	/**
	 * Objetivo: a entrega correta da os votos do conselheiro.
	 * Cenario: o bonus de Karakorum e trocado por DUAS_MOEDAS (nao da voto).
	 * Esperado: o jogador fica com 2 votos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void entregaEmKarakorumDaDoisVotos() {
		jogo.getMapa().getConselheiro(Mapa.KARAKORUM).setBonus(PecaBonus.DUAS_MOEDAS);
		jogo.entregarTesouros(Mapa.KARAKORUM, Arrays.asList("SEDA", "FERRO", "ESPECIARIA"));
		assertEquals("Karakorum da 2 votos por entrega", 2, jogo.getVotos("AZUL"));
	}

	/**
	 * Objetivo: entrega diferente da demanda e recusada.
	 * Cenario: o jogador oferece SEDA, SEDA e FERRO (ele tem SEDA, FERRO e
	 * ESPECIARIA, e falta uma segunda SEDA).
	 * Esperado: a entrega e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void entregaDiferenteDaDemandaERecusada() {
		azul.adicionarTesouro(TipoTesouro.SEDA);
		assertFalse("Karakorum nao pede 2 sedas",
				jogo.entregarTesouros(Mapa.KARAKORUM, Arrays.asList("SEDA", "SEDA", "FERRO")));
	}

	/**
	 * Objetivo: o primeiro a entregar num conselheiro recebe a peca de bonus.
	 * Esperado: o jogador passa a ter 1 bonus obtido.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void primeiraEntregaRecebeBonus() {
		jogo.entregarTesouros(Mapa.KARAKORUM, Arrays.asList("SEDA", "FERRO", "ESPECIARIA"));
		assertEquals("O bonus de Karakorum foi recolhido", 1, azul.getBonusObtidos());
	}

	/**
	 * Objetivo: a segunda entrega no mesmo conselheiro nao da novo bonus.
	 * Cenario: o jogador entrega duas vezes em Karakorum.
	 * Esperado: continua com 1 bonus obtido.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void segundaEntregaNaoRecebeBonus() {
		jogo.entregarTesouros(Mapa.KARAKORUM, Arrays.asList("SEDA", "FERRO", "ESPECIARIA"));
		azul.adicionarTesouro(TipoTesouro.SEDA);
		azul.adicionarTesouro(TipoTesouro.FERRO);
		azul.adicionarTesouro(TipoTesouro.ESPECIARIA);
		jogo.entregarTesouros(Mapa.KARAKORUM, Arrays.asList("SEDA", "FERRO", "ESPECIARIA"));
		assertEquals("So a primeira entrega da bonus", 1, azul.getBonusObtidos());
	}

	/**
	 * Objetivo: a carta de conselheiro aberto da voto ao primeiro que a cumpre
	 * e sai do jogo.
	 * Cenario: carta QUATRO_ESPADAS revelada; azul tem 3 espadas e ganha a 4a
	 * com a habilidade de Chagatai (troca 1 cavalo por 1 espada).
	 * Esperado: a carta sai da lista de cartas abertas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void cartaAbertaSaiDoJogoAoSerCumprida() {
		jogo.getCartasAbertasInternas().add(CartaConselheiroAberto.QUATRO_ESPADAS);
		azul.adicionarTributo(TipoTributo.ESPADA, 3);
		jogo.ativarColuna("AMARELA");
		jogo.trocarCavaloPorEspada();
		assertTrue("A carta ja foi cumprida", jogo.getCartasAbertas().isEmpty());
	}

	/**
	 * Objetivo: a habilidade de Chagatai so pode ser usada 1x por turno.
	 * Esperado: a segunda troca no mesmo turno e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void chagataiTrocaCavaloSoUmaVezPorTurno() {
		jogo.ativarColuna("AMARELA");
		jogo.trocarCavaloPorEspada();
		assertFalse("Habilidade ja usada neste turno", jogo.trocarCavaloPorEspada());
	}
}
