package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

/**
 * Ataque a cidades e conquista (manual, pagina 10) e habilidade de Ogedei.
 *
 * Jogador da vez: AZUL/CHAGATAI, colocado numa parada adjacente a uma das
 * cidades reveladas na preparacao.
 */
public class CidadeTest {
	private static final int TEMPO_LIMITE = 1000;

	private Jogo jogo;
	private Cidade cidade;

	@Before
	public void criarPartida() {
		jogo = Cenario.doisJogadores();
		cidade = jogo.getMapa().getCidade(jogo.getCidadesReveladas().get(0));
		String parada = jogo.getMapa().paradasAdjacentesACidade(cidade.getNome()).get(0);
		Cenario.posicionarJogadorDaVez(jogo, parada);
	}

	private String tesouro(int i) {
		return cidade.getTesouros().get(i).name();
	}

	/**
	 * Objetivo: o 2o tesouro da mesma cidade no turno custa 2 espadas.
	 * Cenario: o jogador tem 3 espadas e toma 2 tesouros (1 + 2 espadas).
	 * Esperado: nao sobra nenhuma espada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void segundoTesouroDaMesmaCidadeCustaDuasEspadas() {
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 3);
		jogo.atacarCidade(cidade.getNome(), tesouro(0));
		jogo.atacarCidade(cidade.getNome(), tesouro(0));
		assertEquals("1 espada pelo 1o tesouro e 2 pelo 2o", 0, jogo.getTributos("AZUL", "ESPADA"));
	}

	/**
	 * Objetivo: sem espadas suficientes o ataque e recusado.
	 * Cenario: 2 espadas; o 1o ataque gasta 1 e o 2o exigiria 2.
	 * Esperado: o 2o ataque e recusado.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void segundoAtaqueSemEspadasSuficientesERecusado() {
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 2);
		jogo.atacarCidade(cidade.getNome(), tesouro(0));
		assertFalse("Resta 1 espada e o 2o tesouro custa 2", jogo.atacarCidade(cidade.getNome(), tesouro(0)));
	}

	/**
	 * Objetivo: nao se ataca cidade que nao e adjacente a uma parada visitada.
	 * Cenario: o peao e recolocado em Karakorum, que nao e adjacente a cidades.
	 * Esperado: o ataque e recusado.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void naoAtacaCidadeDistante() {
		Cenario.posicionarJogadorDaVez(jogo, Mapa.KARAKORUM);
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 1);
		assertFalse("Karakorum nao e adjacente a cidade", jogo.atacarCidade(cidade.getNome(), tesouro(0)));
	}

	/**
	 * Objetivo: tomar o ultimo tesouro conquista a cidade e revela outra.
	 * Cenario: a cidade fica com 1 tesouro so; o jogador tem 1 espada.
	 * Esperado: continuam 3 cidades disponiveis para ataque.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void conquistarCidadeRevelaANova() {
		while (cidade.getTesouros().size() > 1) {
			cidade.getTesouros().remove(0);
		}
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 1);
		jogo.atacarCidade(cidade.getNome(), tesouro(0));
		assertEquals("A cidade conquistada e substituida", 3, jogo.getCidadesReveladas().size());
	}

	/**
	 * Objetivo: o conquistador coloca um yurt no meio da cidade.
	 * Cenario: igual ao anterior.
	 * Esperado: o jogador fica com 11 yurts disponiveis.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void conquistarCidadeGastaUmYurt() {
		while (cidade.getTesouros().size() > 1) {
			cidade.getTesouros().remove(0);
		}
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 1);
		jogo.atacarCidade(cidade.getNome(), tesouro(0));
		assertEquals("12 yurts - 1 colocado na cidade", 11, jogo.getYurtsDisponiveis("AZUL"));
	}

	/**
	 * Objetivo: Ogedei nao pode pegar com moeda o ultimo tesouro de uma cidade.
	 * Cenario: vez do vermelho (Ogedei), com moeda; a cidade tem 1 tesouro.
	 * Esperado: a acao e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void ogedeiNaoPegaUltimoTesouro() {
		Cenario.jogarTurnoVazio(jogo);
		while (cidade.getTesouros().size() > 1) {
			cidade.getTesouros().remove(0);
		}
		assertFalse("E o ultimo tesouro da cidade", jogo.comprarTesouroComMoeda(cidade.getNome(), tesouro(0)));
	}
}
