package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Regressoes dos problemas encontrados na revisao da primeira entrega.
 * Cada teste cria uma partida independente. A preparacao verifica o sucesso
 * das operacoes anteriores para que uma recusa acidental nao mascare o defeito.
 */
public class RegressaoTest {
	private static final int TEMPO_LIMITE = 1000;

	/**
	 * Objetivo: Altani usa somente um peao por turno, mesmo sem movimento.
	 * Cenario: em um turno anterior o peao 0 chega a R2. No turno seguinte ele
	 * pega o tributo de RV1, enquanto o peao 1 continua em Karakorum.
	 * Esperado: a troca para o peao 1 e recusada depois da coleta.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniNaoTrocaPeaoDepoisDePegarTributoSemMover() {
		Jogo jogo = jogoPreparado("ALTANI");
		exigir(jogo.ativarColuna("VERDE"), "Altani deve ativar a verde no primeiro turno");
		exigir(jogo.moverPara("R2"), "O peao 0 deve chegar a R2 na preparacao");
		exigir(jogo.finalizarTurno(), "O primeiro turno de Altani deve terminar");
		exigir(jogo.ativarColuna("VERDE"), "Ogedei deve ativar a verde");
		exigir(jogo.finalizarTurno(), "O turno de Ogedei deve terminar");
		exigir(jogo.ativarColuna("VERMELHA"), "Altani deve receber dois punhos");
		exigir(jogo.pegarTributo("RV1"), "A coleta em RV1 deve ser executada pelo peao 0");
		assertFalse("Depois de agir com o peao 0, Altani nao pode trocar para o peao 1",
				jogo.escolherPeao(1));
	}

	/**
	 * Objetivo: a restricao de Altani nao bloqueia a escolha antes de agir.
	 * Cenario: a coluna vermelha foi ativada, mas nenhuma acao foi executada.
	 * Esperado: a escolha do segundo peao ainda e permitida.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniPodeEscolherPeaoAntesDeAgir() {
		Jogo jogo = jogoPreparado("ALTANI");
		exigir(jogo.ativarColuna("VERMELHA"), "A coluna deve ser ativada antes da escolha");
		assertTrue("A ativacao da coluna ainda permite escolher qual peao vai agir", jogo.escolherPeao(1));
	}

	/**
	 * Objetivo: recursos virtuais contam imediatamente para cartas abertas.
	 * Cenario: tres espadas fisicas e uma melhoria antiga de espada na amarela;
	 * apenas QUATRO_ESPADAS esta revelada.
	 * Esperado: ativar a coluna concede o voto, sem outra acao intermediaria.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void ativarColunaConcedeVotoPorQuartaEspadaVirtual() {
		Jogo jogo = jogoPreparado("CHAGATAI");
		deixarSomenteCarta(jogo, CartaConselheiroAberto.QUATRO_ESPADAS);
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 3);
		colocarMelhoriaAntiga(jogo, new Melhoria(TipoMelhoria.ESPADA, CorColuna.AMARELA));
		exigir(jogo.ativarColuna("AMARELA"), "A melhoria antiga deve ser ativada");
		assertEquals("Tres espadas fisicas mais uma virtual cumprem a carta durante a ativacao", 1,
				jogo.getVotos("AZUL"));
	}

	/**
	 * Objetivo: a quantidade total inclui tesouros virtuais na ativacao.
	 * Cenario: tres sedas fisicas e uma melhoria antiga que fornece BOI virtual.
	 * Esperado: QUATRO_TESOUROS concede um voto imediatamente.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void ativarColunaConcedeVotoPorQuartoTesouroVirtual() {
		Jogo jogo = jogoPreparado("CHAGATAI");
		deixarSomenteCarta(jogo, CartaConselheiroAberto.QUATRO_TESOUROS);
		for (int i = 0; i < 3; i++) {
			jogo.jogadorDaVez().adicionarTesouro(TipoTesouro.SEDA);
		}
		colocarMelhoriaAntiga(jogo, Melhoria.deTesouro(TipoTesouro.BOI));
		exigir(jogo.ativarColuna("AMARELA"), "O boi virtual deve ser disponibilizado");
		assertEquals("Tres tesouros fisicos mais um virtual cumprem a carta durante a ativacao", 1,
				jogo.getVotos("AZUL"));
	}

	/**
	 * Objetivo: a diversidade de tesouros tambem inclui recursos virtuais.
	 * Cenario: um tesouro fisico de cada tipo, exceto BOI; a melhoria antiga da
	 * coluna amarela fornece o quinto tipo.
	 * Esperado: CINCO_TESOUROS_DIFERENTES concede um voto na ativacao.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void ativarColunaConcedeVotoPeloQuintoTipoDeTesouroVirtual() {
		Jogo jogo = jogoPreparado("CHAGATAI");
		deixarSomenteCarta(jogo, CartaConselheiroAberto.CINCO_TESOUROS_DIFERENTES);
		for (TipoTesouro t : TipoTesouro.values()) {
			if (t != TipoTesouro.BOI) {
				jogo.jogadorDaVez().adicionarTesouro(t);
			}
		}
		colocarMelhoriaAntiga(jogo, Melhoria.deTesouro(TipoTesouro.BOI));
		exigir(jogo.ativarColuna("AMARELA"), "O quinto tipo deve ficar disponivel como tesouro virtual");
		assertEquals("O quinto tipo virtual completa a diversidade exigida pela carta", 1, jogo.getVotos("AZUL"));
	}

	/**
	 * Objetivo: o esgotamento das melhorias nao cria uma escolha impossivel.
	 * Cenario: a linha 0 ja tem melhorias vermelha e verde; resta apenas uma
	 * melhoria amarela em toda a area e pilha, comprada para completar a linha.
	 * Esperado: o turno pode terminar sem exigir uma melhoria gratis inexistente.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void comprarUltimaMelhoriaAoCompletarLinhaNaoBloqueiaFimDoTurno() {
		Jogo jogo = jogoPreparado("CHAGATAI");
		AreaMelhorias area = jogo.getAreaMelhorias();
		for (int i = 0; i < AreaMelhorias.TAMANHO; i++) {
			area.definirEspaco(i, null);
		}
		while (area.retirarDaPilha() != null) {
			// O cenario exige que nao sobre nenhuma melhoria oculta.
		}
		area.definirEspaco(4, new Melhoria(TipoMelhoria.PUNHO, CorColuna.AMARELA));
		TabuleiroJogador tabuleiro = jogo.jogadorDaVez().getTabuleiro();
		tabuleiro.colocar(CorColuna.VERMELHA, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERMELHA));
		tabuleiro.colocar(CorColuna.VERDE, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERDE));
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.MOEDA, 1);
		exigir(jogo.ativarColuna("AMARELA"), "A coluna amarela nao deixa Khan pendente");
		exigir(jogo.comprarMelhoria(4, "AMARELA"), "A ultima melhoria deve ser comprada e completar a linha");
		assertTrue("Sem melhorias restantes, a recompensa indisponivel nao impede encerrar o turno",
				jogo.finalizarTurno());
	}

	/**
	 * Objetivo: Jochi escolhe sua melhoria inicial durante a preparacao.
	 * Cenario: ambas as secretas foram escolhidas, mas as duas melhorias de
	 * Jochi ainda aguardam a escolha manual.
	 * Esperado: ativar uma coluna e recusado enquanto essa escolha esta pendente.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void jochiPrecisaEscolherMelhoriaInicialAntesDeAtivarColuna() {
		Jogo jogo = novoJogo("JOCHI", "OGEDEI");
		escolherSecretas(jogo);
		assertFalse("A preparacao de Jochi exige escolher uma melhoria antes da primeira ativacao",
				jogo.ativarColuna("AMARELA"));
	}

	/**
	 * Objetivo: uma ativacao recusada nao escolhe silenciosamente por Jochi.
	 * Cenario: a primeira ativacao e tentada com a melhoria inicial pendente.
	 * Esperado: as duas opcoes continuam oferecidas ao jogador.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void ativacaoRecusadaPreservaAsDuasOpcoesIniciaisDeJochi() {
		Jogo jogo = novoJogo("JOCHI", "OGEDEI");
		escolherSecretas(jogo);
		jogo.ativarColuna("AMARELA");
		assertEquals("A tentativa de ativacao nao pode selecionar automaticamente a primeira opcao", 2,
				jogo.jogador("AZUL").getMelhoriasIniciaisOferecidas().size());
	}

	/**
	 * Objetivo: a melhoria inicial escolhida pelo jogador e respeitada.
	 * Cenario: Jochi escolhe explicitamente a segunda melhoria e inicia seu turno.
	 * Esperado: a coluna branca contem exatamente a melhoria do indice 1.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void jochiPodeEscolherSegundaMelhoriaInicialNaPreparacao() {
		Jogo jogo = novoJogo("JOCHI", "OGEDEI");
		escolherSecretas(jogo);
		Melhoria segunda = jogo.jogador("AZUL").getMelhoriasIniciaisOferecidas().get(1);
		exigir(jogo.escolherMelhoriaInicial("AZUL", 1), "A escolha manual do indice 1 deve ser aceita");
		exigir(jogo.ativarColuna("AMARELA"), "A partida deve comecar depois da escolha de Jochi");
		assertSame("A coluna branca deve manter a melhoria inicial escolhida pelo jogador", segunda,
				jogo.jogador("AZUL").getTabuleiro().getMelhoria(CorColuna.BRANCA, 0));
	}

	/**
	 * Objetivo: a preparacao deve terminar para todos antes do primeiro turno.
	 * Cenario: Azul escolheu sua secreta, mas Vermelho ainda nao escolheu.
	 * Esperado: Azul nao pode iniciar uma coluna enquanto a escolha de outro
	 * jogador continua pendente.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void cartaSecretaPendenteDeOutroJogadorImpedePrimeiraAtivacao() {
		Jogo jogo = novoJogo("CHAGATAI", "OGEDEI");
		exigir(jogo.escolherConselheiroSecreto("AZUL", 0), "Azul deve escolher sua propria secreta");
		assertFalse("A carta secreta de Vermelho deve ser escolhida antes de Azul iniciar a partida",
				jogo.ativarColuna("AMARELA"));
	}

	/**
	 * Objetivo: a preparacao de Jochi e obrigatoria mesmo se ele nao joga primeiro.
	 * Cenario: Azul e Chagatai; Vermelho e Jochi. As secretas foram escolhidas,
	 * mas a melhoria inicial de Vermelho continua pendente.
	 * Esperado: Azul nao pode iniciar a partida antes dessa escolha.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void melhoriaInicialPendenteDeOutroJogadorImpedePrimeiraAtivacao() {
		Jogo jogo = novoJogo("CHAGATAI", "JOCHI");
		escolherSecretas(jogo);
		assertFalse("A melhoria inicial de Jochi deve ser escolhida antes do primeiro turno de Azul",
				jogo.ativarColuna("AMARELA"));
	}

	/**
	 * Objetivo: uma acao extra nao contorna a preparacao global da partida.
	 * Cenario: Azul tem duas moedas e uma melhoria verde compravel, mas Vermelho
	 * ainda nao escolheu sua carta secreta.
	 * Esperado: a compra e recusada e as duas moedas continuam com Azul.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void compraAntesDeConcluirPreparacaoERecusadaSemGastarMoedas() {
		Jogo jogo = novoJogo("CHAGATAI", "OGEDEI");
		exigir(jogo.escolherConselheiroSecreto("AZUL", 0), "A escolha de Azul deve estar concluida");
		jogo.jogador("AZUL").adicionarTributo(TipoTributo.MOEDA, 1);
		jogo.getAreaMelhorias().definirEspaco(4, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERDE));
		boolean compra = jogo.comprarMelhoria(4, "VERDE");
		assertEquals("A compra durante a preparacao deve devolver false e preservar as duas moedas",
				Arrays.asList(false, 2), Arrays.asList(compra, jogo.getTributos("AZUL", "MOEDA")));
	}

	/**
	 * Objetivo: o ataque fixa o peao usado por Altani, mesmo sem movimento.
	 * Cenario: o peao 0 comeca o turno adjacente a uma cidade revelada e toma
	 * um tesouro com uma espada fisica; o peao 1 permanece em Karakorum.
	 * Esperado: depois do ataque nao e permitido trocar para o outro peao.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniNaoTrocaPeaoDepoisDeAtacarSemMover() {
		Jogo jogo = jogoPreparado("ALTANI");
		Cidade cidade = jogo.getMapa().getCidade(jogo.getCidadesReveladas().get(0));
		posicionarPeaoDaVez(jogo, jogo.getMapa().paradasAdjacentesACidade(cidade.getNome()).get(0));
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 1);
		exigir(jogo.ativarColuna("VERDE"), "Altani deve ativar a verde sem mover seu peao");
		exigir(jogo.atacarCidade(cidade.getNome(), cidade.getTesouros().get(0).name()),
				"O peao 0 deve atacar a cidade adjacente");
		assertFalse("O ataque executado pelo peao 0 impede trocar para o peao 1 no mesmo turno",
				jogo.escolherPeao(1));
	}

	/**
	 * Objetivo: construir um yurt fixa o peao usado por Altani no turno.
	 * Cenario: o peao 0 comeca em R1, recebe um tributo de yurt e constroi
	 * nessa parada sem executar nenhum movimento.
	 * Esperado: depois da construcao nao e permitido trocar para o peao 1.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniNaoTrocaPeaoDepoisDeConstruirYurtSemMover() {
		Jogo jogo = jogoPreparado("ALTANI");
		posicionarPeaoDaVez(jogo, "R1");
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.YURT, 1);
		exigir(jogo.ativarColuna("VERDE"), "Altani deve ativar a verde sem mover seu peao");
		exigir(jogo.construirYurt("R1"), "O peao 0 deve construir um yurt na parada inicial");
		assertFalse("A construcao executada pelo peao 0 impede trocar para o peao 1 no mesmo turno",
				jogo.escolherPeao(1));
	}

	/**
	 * Objetivo: uma entrega ao conselheiro fixa o peao usado por Altani.
	 * Cenario: o peao 0 entrega seda, ferro e especiaria em Karakorum, sem mover.
	 * Esperado: depois da entrega nao e permitido trocar para o peao 1.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniNaoTrocaPeaoDepoisDeEntregarTesourosSemMover() {
		Jogo jogo = jogoPreparado("ALTANI");
		jogo.jogadorDaVez().adicionarTesouro(TipoTesouro.SEDA);
		jogo.jogadorDaVez().adicionarTesouro(TipoTesouro.FERRO);
		jogo.jogadorDaVez().adicionarTesouro(TipoTesouro.ESPECIARIA);
		exigir(jogo.ativarColuna("VERDE"), "Altani deve ativar a verde em Karakorum");
		exigir(jogo.entregarTesouros(Jogo.KARAKORUM, Arrays.asList("SEDA", "FERRO", "ESPECIARIA")),
				"O peao 0 deve entregar os tres tesouros pedidos pelo conselheiro");
		assertFalse("A entrega executada pelo peao 0 impede trocar para o peao 1 no mesmo turno",
				jogo.escolherPeao(1));
	}

	/**
	 * Objetivo: a restricao de escolha do peao termina junto com o turno.
	 * Cenario: Altani coleta em RV1 sem mover e fica impedida de trocar. Seu
	 * turno e o turno de Ogedei terminam, e uma nova coluna de Altani e ativada.
	 * Esperado: no novo turno ela pode escolher o peao 1 antes de agir.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void novoTurnoDeAltaniPermiteTrocarPeaoBloqueadoNoTurnoAnterior() {
		Jogo jogo = jogoPreparado("ALTANI");
		posicionarPeaoDaVez(jogo, "R2");
		exigir(jogo.ativarColuna("VERDE"), "Altani deve receber uma acao de coleta");
		exigir(jogo.pegarTributo("RV1"), "O peao 0 deve agir no primeiro turno");
		exigir(!jogo.escolherPeao(1), "A troca deve estar bloqueada antes de encerrar o turno");
		exigir(jogo.finalizarTurno(), "O turno bloqueado de Altani deve terminar");
		exigir(jogo.ativarColuna("VERDE"), "Ogedei deve iniciar seu turno");
		exigir(jogo.finalizarTurno(), "O turno intermediario de Ogedei deve terminar");
		exigir(jogo.ativarColuna("AMARELA"), "Altani deve iniciar um novo turno");
		assertTrue("A acao do turno anterior nao impede escolher outro peao no novo turno", jogo.escolherPeao(1));
	}

	/**
	 * Objetivo: a API de Jochi informa as opcoes sem expor listas ou mapas internos.
	 * Cenario: sao oferecidas uma espada verde e uma melhoria de boi amarela.
	 * Depois de consultar, o chamador altera os mapas retornados e esvazia a lista.
	 * Esperado: a consulta inicial e a nova consulta preservam os dados originais.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultaPublicaDasMelhoriasIniciaisDeJochiDevolveCopiasCorretas() {
		Jogo jogo = novoJogo("JOCHI", "OGEDEI");
		List<Melhoria> oferecidas = jogo.jogador("AZUL").getMelhoriasIniciaisOferecidas();
		oferecidas.clear();
		oferecidas.add(new Melhoria(TipoMelhoria.ESPADA, CorColuna.VERDE));
		oferecidas.add(Melhoria.deTesouro(TipoTesouro.BOI));
		List<Map<String, String>> esperadas = Arrays.asList(dadosMelhoria("ESPADA", "VERDE", ""),
				dadosMelhoria("TESOURO", "AMARELA", "BOI"));
		List<Map<String, String>> consulta = jogo.getMelhoriasIniciaisOferecidas("AZUL");
		List<Map<String, String>> dadosDaPrimeiraConsulta = new ArrayList<Map<String, String>>();
		for (Map<String, String> dados : consulta) {
			dadosDaPrimeiraConsulta.add(new HashMap<String, String>(dados));
		}
		consulta.get(0).put("tipo", "CAVALOS");
		consulta.get(1).put("tesouro", "SEDA");
		consulta.clear();
		assertEquals("As consultas devem informar tipo, cor e tesouro sem compartilhar o estado da partida",
				Arrays.asList(esperadas, esperadas),
				Arrays.asList(dadosDaPrimeiraConsulta, jogo.getMelhoriasIniciaisOferecidas("AZUL")));
	}

	/**
	 * Objetivo: uma recompensa recebida nao deixa outra obrigatoria sem peca.
	 * Cenario: duas compras amarelas completam as linhas 0 e 1, gerando duas
	 * melhorias gratis pendentes, mas resta somente uma melhoria no mercado.
	 * Esperado: a ultima peca vai para a branca da linha 0, a branca da linha 1
	 * fica vazia, a pendencia impossivel e encerrada e o turno pode terminar.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void duasMelhoriasGratisComUmaPecaRestanteNaoBloqueiamFimDoTurno() {
		Jogo jogo = jogoPreparado("CHAGATAI");
		jogo.getCartasAbertasInternas().clear();
		AreaMelhorias area = jogo.getAreaMelhorias();
		for (int i = 0; i < AreaMelhorias.TAMANHO; i++) {
			area.definirEspaco(i, null);
		}
		while (area.retirarDaPilha() != null) {
			// As tres pecas reveladas abaixo sao as unicas restantes.
		}
		area.definirEspaco(2, new Melhoria(TipoMelhoria.PUNHO, CorColuna.AMARELA));
		area.definirEspaco(3, new Melhoria(TipoMelhoria.PUNHO, CorColuna.AMARELA));
		Melhoria ultima = new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERDE);
		area.definirEspaco(4, ultima);
		TabuleiroJogador tabuleiro = jogo.jogadorDaVez().getTabuleiro();
		for (int linha = 0; linha < 2; linha++) {
			tabuleiro.colocar(CorColuna.VERMELHA, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERMELHA));
			tabuleiro.colocar(CorColuna.VERDE, new Melhoria(TipoMelhoria.PUNHO, CorColuna.VERDE));
		}
		jogo.jogadorDaVez().adicionarTributo(TipoTributo.MOEDA, 3);
		exigir(jogo.ativarColuna("AMARELA"), "A coluna nao deve deixar Khan pendente");
		exigir(jogo.comprarMelhoria(2, "AMARELA"), "A primeira compra deve completar a linha 0");
		exigir(jogo.comprarMelhoria(3, "AMARELA"), "A segunda compra deve completar a linha 1");
		exigir(jogo.getMelhoriasGratisPendentes() == 2, "As duas linhas devem gerar recompensas antes da escolha");
		boolean recebeuUltima = jogo.pegarMelhoriaGratis(4);
		int pendentes = jogo.getMelhoriasGratisPendentes();
		boolean terminouTurno = jogo.finalizarTurno();
		assertEquals("A ultima peca atende a primeira linha e libera o turno sem inventar a segunda recompensa",
				Arrays.asList(true, ultima, null, 0, true),
				Arrays.asList(recebeuUltima, tabuleiro.getMelhoria(CorColuna.BRANCA, 0),
						tabuleiro.getMelhoria(CorColuna.BRANCA, 1), pendentes, terminouTurno));
	}

	private static Jogo novoJogo(String primeiroHerdeiro, String segundoHerdeiro) {
		return new Jogo(Arrays.asList("AZUL", "VERMELHO"), Arrays.asList(primeiroHerdeiro, segundoHerdeiro), 42L);
	}

	private static Jogo jogoPreparado(String primeiroHerdeiro) {
		Jogo jogo = novoJogo(primeiroHerdeiro, "OGEDEI");
		escolherSecretas(jogo);
		return jogo;
	}

	private static void escolherSecretas(Jogo jogo) {
		exigir(jogo.escolherConselheiroSecreto("AZUL", 0), "Azul deve escolher sua carta secreta na preparacao");
		exigir(jogo.escolherConselheiroSecreto("VERMELHO", 0), "Vermelho deve escolher sua carta secreta na preparacao");
	}

	private static void deixarSomenteCarta(Jogo jogo, CartaConselheiroAberto carta) {
		jogo.getCartasAbertasInternas().clear();
		jogo.getCartasAbertasInternas().add(carta);
	}

	private static void colocarMelhoriaAntiga(Jogo jogo, Melhoria melhoria) {
		melhoria.setTurnoAquisicao(0);
		jogo.jogadorDaVez().getTabuleiro().colocar(melhoria.getCor(), melhoria);
	}

	/** Define a posicao inicial de um cenario; nao registra movimento no turno. */
	private static void posicionarPeaoDaVez(Jogo jogo, String parada) {
		jogo.jogadorDaVez().setPosicao(parada);
		jogo.getTurno().reiniciarParadasVisitadas(parada);
	}

	private static Map<String, String> dadosMelhoria(String tipo, String cor, String tesouro) {
		Map<String, String> dados = new HashMap<String, String>();
		dados.put("tipo", tipo);
		dados.put("cor", cor);
		dados.put("tesouro", tesouro);
		return dados;
	}

	/** Valida apenas a preparacao; a assertiva final de cada teste avalia a regra. */
	private static void exigir(boolean sucesso, String mensagem) {
		if (!sucesso) {
			throw new IllegalStateException("Falha na preparacao do teste: " + mensagem);
		}
	}
}
