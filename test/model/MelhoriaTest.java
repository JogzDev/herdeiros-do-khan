package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tabuleiro de jogador: colunas, compra de melhorias e linha completa
 * (manual, paginas 6, 11 e 12).
 */
public class MelhoriaTest {
	private static final int TEMPO_LIMITE = 1000;

	private Jogo jogo;
	private Jogador azul;

	@Before
	public void criarPartida() {
		jogo = Cenario.doisJogadores();
		azul = jogo.jogador("AZUL");
		azul.adicionarTributo(TipoTributo.MOEDA, 5);
	}

	/**
	 * Objetivo: a melhoria tem de ir para a coluna da mesma cor.
	 * Cenario: uma melhoria verde no espaco 4 da area; tentativa de por na vermelha.
	 * Esperado: a compra e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void compraExigeColunaDaMesmaCor() {
		jogo.getAreaMelhorias().definirEspaco(4, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERDE));
		assertFalse("Melhoria verde nao vai na coluna vermelha", jogo.comprarMelhoria(4, "VERMELHA"));
	}

	/**
	 * Objetivo: a compra custa 2 moedas.
	 * Cenario: o jogador tem 6 moedas (1 inicial + 5) e compra uma melhoria verde.
	 * Esperado: ficam 4 moedas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void compraCustaDuasMoedas() {
		jogo.getAreaMelhorias().definirEspaco(4, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERDE));
		jogo.comprarMelhoria(4, "VERDE");
		assertEquals("6 moedas - 2 da compra", 4, jogo.getTributos("AZUL", "MOEDA"));
	}

	/**
	 * Objetivo: melhoria comprada no turno nao pode ser usada no mesmo turno.
	 * Cenario: compra-se uma melhoria de cavalos para a coluna amarela e depois
	 * ativa-se a amarela (2 cavalos iniciais de Chagatai).
	 * Esperado: o jogador tem so os 2 movimentos iniciais.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void melhoriaCompradaNaoValeNoMesmoTurno() {
		jogo.getAreaMelhorias().definirEspaco(4, new Melhoria(TipoMelhoria.CAVALOS, CorColuna.AMARELA));
		jogo.comprarMelhoria(4, "AMARELA");
		jogo.ativarColuna("AMARELA");
		assertEquals("Os 2 cavalos da melhoria so valem nos proximos turnos", 2, jogo.getMovimentosRestantes());
	}

	/**
	 * Objetivo: melhoria adquirida em turno anterior soma acoes a coluna.
	 * Cenario: melhoria de cavalos ja na coluna amarela, adquirida no turno 0.
	 * Esperado: 2 cavalos iniciais + 2 da melhoria = 4 movimentos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void melhoriaAntigaSomaAcoes() {
		Melhoria m = new Melhoria(TipoMelhoria.CAVALOS, CorColuna.AMARELA);
		m.setTurnoAquisicao(0);
		azul.getTabuleiro().colocar(CorColuna.AMARELA, m);
		jogo.ativarColuna("AMARELA");
		assertEquals("2 iniciais + 2 da melhoria", 4, jogo.getMovimentosRestantes());
	}

	/**
	 * Objetivo: completar uma linha da direito a uma melhoria gratis.
	 * Cenario: a linha 0 ja tem melhorias vermelha e verde; compra-se uma amarela.
	 * Esperado: ha 1 melhoria gratis pendente.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void completarLinhaDaMelhoriaGratis() {
		azul.getTabuleiro().colocar(CorColuna.VERMELHA, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERMELHA));
		azul.getTabuleiro().colocar(CorColuna.VERDE, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERDE));
		jogo.getAreaMelhorias().definirEspaco(4, new Melhoria(TipoMelhoria.PUNHO, CorColuna.AMARELA));
		jogo.comprarMelhoria(4, "AMARELA");
		assertEquals("A linha 0 foi completada", 1, jogo.getMelhoriasGratisPendentes());
	}

	/**
	 * Objetivo: Jochi nao ganha melhoria ao completar a 1a linha, pois o espaco
	 * da coluna branca ja esta ocupado pela melhoria inicial dele.
	 * Cenario: partida com JOCHI como 1o jogador.
	 * Esperado: nenhuma melhoria gratis pendente.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void jochiNaoGanhaMelhoriaNaPrimeiraLinha() {
		Jogo comJochi = Cenario.comHerdeiros("JOCHI", "OGEDEI");
		Jogador jochi = comJochi.jogador("AZUL");
		jochi.adicionarTributo(TipoTributo.MOEDA, 2);
		jochi.getTabuleiro().colocar(CorColuna.VERMELHA, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERMELHA));
		jochi.getTabuleiro().colocar(CorColuna.VERDE, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERDE));
		comJochi.getAreaMelhorias().definirEspaco(4, new Melhoria(TipoMelhoria.PUNHO, CorColuna.AMARELA));
		comJochi.comprarMelhoria(4, "AMARELA");
		assertEquals("O espaco da coluna branca ja esta ocupado", 0, comJochi.getMelhoriasGratisPendentes());
	}

	/**
	 * Objetivo: uma coluna usada so volta a ficar disponivel depois da branca.
	 * Cenario: azul usa a amarela; vermelho joga; azul tenta a amarela de novo.
	 * Esperado: a ativacao e recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void colunaUsadaFicaIndisponivel() {
		jogo.ativarColuna("AMARELA");
		jogo.finalizarTurno();
		Cenario.jogarTurnoVazio(jogo);
		assertFalse("A peca de ativacao ainda esta na amarela", jogo.ativarColuna("AMARELA"));
	}

	/**
	 * Objetivo: a coluna branca recupera todas as pecas de ativacao.
	 * Cenario: azul usa a amarela e depois a branca; no turno seguinte tenta a amarela.
	 * Esperado: a ativacao e aceita.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void colunaBrancaRecuperaAsPecas() {
		jogo.ativarColuna("AMARELA");
		jogo.finalizarTurno();
		Cenario.jogarTurnoVazio(jogo);
		jogo.ativarColuna("BRANCA");
		jogo.moverKhan("PV2");
		jogo.finalizarTurno();
		Cenario.jogarTurnoVazio(jogo);
		assertTrue("A branca devolveu todas as pecas", jogo.ativarColuna("AMARELA"));
	}
}
