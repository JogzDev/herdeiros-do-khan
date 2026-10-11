package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Trilha do Kurultai, fim da partida, pontuacao de influencia, cartas secretas
 * e desempate (manual, paginas 14 a 16 e 19).
 */
public class FimDeJogoTest {
	private static final int TEMPO_LIMITE = 1000;

	/**
	 * Objetivo: o limite da trilha depende do numero de jogadores.
	 * Esperado: com 3 jogadores o fim e disparado em 14 votos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void limiteDaTrilhaComTresJogadores() {
		assertEquals("Manual: 10/14/16/17 votos para 2/3/4/5 jogadores", 14, new TrilhaKurultai(3).getLimite());
	}

	/**
	 * Objetivo: numero de jogadores fora de 2 a 5 e rejeitado.
	 */
	@Test(timeout = TEMPO_LIMITE, expected = IllegalArgumentException.class)
	public void trilhaRejeitaSeisJogadores() {
		new TrilhaKurultai(6);
	}

	/**
	 * Objetivo: atingir o limite de votos dispara o fim do jogo.
	 * Cenario: 2 jogadores, ficha neutra em 9; azul entrega em Karakorum (+2).
	 * Esperado: o fim e disparado.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void atingirLimiteDisparaFim() {
		Jogo jogo = jogoComFichaNeutraEm9();
		entregarEmKarakorum(jogo);
		assertTrue("A ficha neutra chegou a 11 (limite 10)", jogo.isFimDisparado());
	}

	/**
	 * Objetivo: depois do disparo termina-se a rodada e joga-se mais uma.
	 * Cenario: azul (1o jogador) dispara o fim; seguem-se o turno do vermelho
	 * e mais uma rodada completa (azul e vermelho).
	 * Esperado: a partida termina apos o 4o turno, contando o do disparo.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void partidaTerminaAposRodadaFinal() {
		Jogo jogo = jogoComFichaNeutraEm9();
		entregarEmKarakorum(jogo);
		jogo.ativarColuna("AMARELA");
		jogo.finalizarTurno();
		for (int i = 0; i < 3; i++) {
			Cenario.jogarTurnoVazio(jogo);
		}
		assertTrue("Turnos: azul (disparo), vermelho, azul, vermelho", jogo.isTerminado());
	}

	/**
	 * Objetivo: a partida nao termina antes da rodada final completa.
	 * Cenario: igual ao anterior, mas so 3 turnos jogados.
	 * Esperado: a partida ainda nao terminou.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void partidaNaoTerminaAntesDaRodadaFinal() {
		Jogo jogo = jogoComFichaNeutraEm9();
		entregarEmKarakorum(jogo);
		jogo.ativarColuna("AMARELA");
		jogo.finalizarTurno();
		for (int i = 0; i < 2; i++) {
			Cenario.jogarTurnoVazio(jogo);
		}
		assertFalse("Ainda falta o ultimo turno do vermelho", jogo.isTerminado());
	}

	/**
	 * Objetivo: empate no 1o lugar de uma regiao deixa o 2o lugar vazio.
	 * Cenario: Russia com 2 yurts azuis, 2 vermelhos e 1 verde. O verde tem 1
	 * melhoria na coluna amarela, o que lhe daria 1 voto se fosse 2o colocado.
	 * Esperado: o verde nao recebe votos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void empateNoPrimeiroLugarNinguemFicaEmSegundo() {
		Jogo jogo = Cenario.comHerdeiros("CHAGATAI", "OGEDEI", "ALTANI");
		jogo.jogador("VERDE").getTabuleiro().colocar(CorColuna.AMARELA,
				new Melhoria(TipoMelhoria.PUNHO, CorColuna.AMARELA));
		Cenario.colocarYurt(jogo, "AZUL", "R1");
		Cenario.colocarYurt(jogo, "AZUL", "R2");
		Cenario.colocarYurt(jogo, "VERMELHO", "R3");
		Cenario.colocarYurt(jogo, "VERMELHO", "R4");
		Cenario.colocarYurt(jogo, "VERDE", "R5");
		jogo.pontuarInfluencia();
		assertEquals("Com empate no 1o lugar ninguem fica em 2o", 0, jogo.getVotos("VERDE"));
	}

	/**
	 * Objetivo: os votos de influencia dependem das melhorias da coluna da regiao.
	 * Cenario: azul e o unico 1o colocado na Russia e tem 2 melhorias na coluna
	 * amarela (a coluna da Russia).
	 * Esperado: tabela da pagina 15: 1o lugar com 2 melhorias = 2 votos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void influenciaUsaMelhoriasDaColunaDaRegiao() {
		Jogo jogo = Cenario.doisJogadores();
		Jogador azul = jogo.jogador("AZUL");
		azul.getTabuleiro().colocar(CorColuna.AMARELA, new Melhoria(TipoMelhoria.PUNHO, CorColuna.AMARELA));
		azul.getTabuleiro().colocar(CorColuna.AMARELA, new Melhoria(TipoMelhoria.PUNHO, CorColuna.AMARELA));
		Cenario.colocarYurt(jogo, "AZUL", "R1");
		jogo.pontuarInfluencia();
		assertEquals("1o lugar com 2 melhorias na coluna amarela", 2, jogo.getVotos("AZUL"));
	}

	/**
	 * Objetivo: carta Viajante conta a maior rede de yurts conectados.
	 * Cenario: yurts azuis em R1, R2 e R3 (ligados) e em CD2 (isolado).
	 * Esperado: rede de 3 yurts = 1 voto.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void viajanteContaRedeConectada() {
		Jogo jogo = Cenario.doisJogadores();
		Cenario.colocarYurt(jogo, "AZUL", "R1");
		Cenario.colocarYurt(jogo, "AZUL", "R2");
		Cenario.colocarYurt(jogo, "AZUL", "R3");
		Cenario.colocarYurt(jogo, "AZUL", "CD2");
		int votos = CartaConselheiroSecreto.VIAJANTE.votos(jogo.jogador("AZUL"), jogo.getMapa());
		assertEquals("Rede R1-R2-R3 tem 3 yurts", 1, votos);
	}

	/**
	 * Objetivo: empate em votos e decidido por tesouros nao entregues.
	 * Cenario: azul e vermelho com 5 votos; vermelho tem 1 tesouro a mais.
	 * Esperado: o vermelho vence sozinho.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void empateEmVotosDecididoPorTesouros() {
		Jogo jogo = Cenario.doisJogadores();
		jogo.jogador("AZUL").adicionarVotos(5);
		jogo.jogador("VERMELHO").adicionarVotos(5);
		jogo.jogador("VERMELHO").adicionarTesouro(TipoTesouro.BOI);
		List<String> vencedores = jogo.calcularVencedores();
		assertEquals("Vermelho tem mais tesouros", Arrays.asList("VERMELHO"), vencedores);
	}

	// ------------------------------------------------------------ auxiliares

	private static Jogo jogoComFichaNeutraEm9() {
		Jogo jogo = Cenario.doisJogadores();
		jogo.getCartasAbertasInternas().clear();
		jogo.getTrilha().avancar(9);
		jogo.getMapa().getConselheiro(Mapa.KARAKORUM).setBonus(null);
		Jogador azul = jogo.jogador("AZUL");
		azul.adicionarTesouro(TipoTesouro.SEDA);
		azul.adicionarTesouro(TipoTesouro.FERRO);
		azul.adicionarTesouro(TipoTesouro.ESPECIARIA);
		return jogo;
	}

	private static void entregarEmKarakorum(Jogo jogo) {
		jogo.entregarTesouros(Mapa.KARAKORUM, Arrays.asList("SEDA", "FERRO", "ESPECIARIA"));
	}
}
