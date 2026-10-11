package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * API publica do componente Model do jogo Herdeiros do Khan.
 *
 * E a unica classe publica do pacote: View e Controller so conhecem o jogo por
 * meio dela, usando Strings e inteiros como identificadores (cores, herdeiros,
 * paradas, provincias, cidades, tesouros...). Todas as demais classes do pacote
 * representam elementos do jogo e sao nao publicas.
 *
 * Cada operacao de jogada devolve true quando e permitida pelas regras e foi
 * executada, ou false quando nao e permitida (nesse caso o estado nao muda).
 * Cabe ao Controller definir a ordem das etapas de uma jogada; ao Model cabe
 * verificar se cada etapa respeita as regras.
 *
 * Na 3a iteracao esta classe sera transformada em Singleton/Facade.
 */
public final class Jogo {
	/** Identificador da area de melhorias como destino do Khan. */
	public static final String AREA_MELHORIAS = "AREA_MELHORIAS";

	/** Identificador da parada de Karakorum. */
	public static final String KARAKORUM = Mapa.KARAKORUM;

	private final Mapa mapa;
	private final Random random;
	private final List<Jogador> jogadores = new ArrayList<Jogador>();
	private final HashMap<CorJogador, Jogador> jogadoresPorCor = new HashMap<CorJogador, Jogador>();
	private final HashMap<TipoTributo, Integer> estoqueTributos = new HashMap<TipoTributo, Integer>();
	private final List<String> pilhaCidades = new ArrayList<String>();
	private final List<TipoTesouro> pilhaTesouros = new ArrayList<TipoTesouro>();
	private final List<CartaConselheiroAberto> cartasAbertas = new ArrayList<CartaConselheiroAberto>();
	private final TrilhaKurultai trilha;
	private AreaMelhorias areaMelhorias;
	private String posicaoKhan;

	private int indiceDaVez;
	private int numeroTurno;
	private Turno turno;

	private boolean fimDisparado;
	private int turnosRestantes;
	private boolean terminado;
	private boolean partidaIniciada;

	// ================================================================ criacao

	/**
	 * Cria uma partida. A ordem das listas e a ordem de jogada (o primeiro e o
	 * jogador que comeca).
	 *
	 * @param cores     cores dos jogadores (AZUL, AMARELO, CINZA, VERMELHO, VERDE)
	 * @param herdeiros herdeiros escolhidos (ALTANI, JOCHI, CHAGATAI, TOLUI, OGEDEI)
	 */
	public Jogo(List<String> cores, List<String> herdeiros) {
		this(cores, herdeiros, MapaProvisorio.criar(), new Random());
	}

	/** Igual ao construtor anterior, mas com embaralhamento reproduzivel. */
	public Jogo(List<String> cores, List<String> herdeiros, long semente) {
		this(cores, herdeiros, MapaProvisorio.criar(), new Random(semente));
	}

	Jogo(List<String> cores, List<String> herdeiros, Mapa mapa, Random random) {
		if (cores == null || herdeiros == null || cores.size() != herdeiros.size()) {
			throw new IllegalArgumentException("Informe uma cor e um herdeiro por jogador");
		}
		this.mapa = mapa;
		this.random = random;
		this.trilha = new TrilhaKurultai(cores.size());
		criarJogadores(cores, herdeiros);
		prepararPartida();
	}

	private void criarJogadores(List<String> cores, List<String> herdeiros) {
		Set<Herdeiro> usados = new HashSet<Herdeiro>();
		for (int i = 0; i < cores.size(); i++) {
			CorJogador cor = CorJogador.converter(cores.get(i));
			Herdeiro h = Herdeiro.converter(herdeiros.get(i));
			if (cor == null || h == null) {
				throw new IllegalArgumentException("Cor ou herdeiro invalido: " + cores.get(i) + "/" + herdeiros.get(i));
			}
			if (jogadoresPorCor.containsKey(cor) || !usados.add(h)) {
				throw new IllegalArgumentException("Cores e herdeiros nao podem se repetir");
			}
			Jogador j = new Jogador(cor, h, Mapa.KARAKORUM);
			jogadores.add(j);
			jogadoresPorCor.put(cor, j);
		}
	}

	/** Preparacao do jogo, conforme as paginas 3 e 4 do manual. */
	private void prepararPartida() {
		for (TipoTributo t : TipoTributo.values()) {
			estoqueTributos.put(t, t.getQuantidadeTotal());
		}

		// tesouros
		for (TipoTesouro t : TipoTesouro.values()) {
			for (int i = 0; i < TipoTesouro.PECAS_POR_TIPO; i++) {
				pilhaTesouros.add(t);
			}
		}
		Collections.shuffle(pilhaTesouros, random);

		// cidades: 3 reveladas, cada uma com 4 tesouros
		for (Cidade c : mapa.getCidades()) {
			pilhaCidades.add(c.getNome());
		}
		Collections.sort(pilhaCidades);
		Collections.shuffle(pilhaCidades, random);
		for (int i = 0; i < 3; i++) {
			revelarProximaCidade();
		}

		// pecas de bonus nas paradas de conselheiro
		List<PecaBonus> bonus = new ArrayList<PecaBonus>();
		Collections.addAll(bonus, PecaBonus.values());
		Collections.shuffle(bonus, random);
		List<String> paradasConselheiro = new ArrayList<String>();
		for (Conselheiro c : mapa.getConselheiros()) {
			paradasConselheiro.add(c.getParadaId());
		}
		Collections.sort(paradasConselheiro);
		for (int i = 0; i < paradasConselheiro.size() && i < bonus.size(); i++) {
			mapa.getConselheiro(paradasConselheiro.get(i)).setBonus(bonus.get(i));
		}

		// cartas de conselheiro aberto: 4 reveladas
		List<CartaConselheiroAberto> abertas = new ArrayList<CartaConselheiroAberto>();
		Collections.addAll(abertas, CartaConselheiroAberto.values());
		Collections.shuffle(abertas, random);
		cartasAbertas.addAll(abertas.subList(0, CartaConselheiroAberto.CARTAS_POR_PARTIDA));

		// 1 tributo em cada provincia
		List<String> idsProvincias = new ArrayList<String>();
		for (Provincia p : mapa.getProvincias()) {
			idsProvincias.add(p.getId());
		}
		Collections.sort(idsProvincias);
		for (String id : idsProvincias) {
			Provincia p = mapa.getProvincia(id);
			if (retirarDoEstoque(p.getTipo(), 1) == 1) {
				p.adicionarPeca();
			}
		}

		// melhorias: 5 reveladas na area
		List<Melhoria> melhorias = criarMelhorias();
		Collections.shuffle(melhorias, random);
		areaMelhorias = new AreaMelhorias(melhorias);

		// por jogador: 2 cartas secretas e moedas iniciais
		List<CartaConselheiroSecreto> secretas = new ArrayList<CartaConselheiroSecreto>();
		Collections.addAll(secretas, CartaConselheiroSecreto.values());
		Collections.shuffle(secretas, random);
		for (int i = 0; i < jogadores.size(); i++) {
			Jogador j = jogadores.get(i);
			j.getSecretasOferecidas().add(secretas.remove(0));
			j.getSecretasOferecidas().add(secretas.remove(0));
			int moedas = (i < 2) ? 1 : 2;
			j.adicionarTributo(TipoTributo.MOEDA, retirarDoEstoque(TipoTributo.MOEDA, moedas));
			prepararHabilidadeInicial(j);
		}

		numeroTurno = 1;
		indiceDaVez = 0;
		turno = new Turno(numeroTurno, jogadorDaVez().getPosicao());
	}

	private void prepararHabilidadeInicial(Jogador j) {
		if (j.getHerdeiro() == Herdeiro.TOLUI) {
			// Tolui comeca com 1 yurt em Karakorum
			j.usarYurt();
			mapa.getParada(Mapa.KARAKORUM).adicionarYurt(j.getCor());
		} else if (j.getHerdeiro() == Herdeiro.JOCHI) {
			// Jochi pega 2 melhorias ao acaso e coloca uma na coluna branca
			for (int i = 0; i < 2; i++) {
				Melhoria m = areaMelhorias.retirarDaPilha();
				if (m != null) {
					j.getMelhoriasIniciaisOferecidas().add(m);
				}
			}
		}
	}

	/**
	 * 44 melhorias. As quantidades por tipo vem do manual; a distribuicao das
	 * cores dentro de cada tipo e uma suposicao (o manual nao a informa).
	 */
	private static List<Melhoria> criarMelhorias() {
		List<Melhoria> lista = new ArrayList<Melhoria>();
		CorColuna[] cores = CorColuna.coloridas();
		TipoMelhoria[] tipos = { TipoMelhoria.CAVALOS, TipoMelhoria.PUNHO, TipoMelhoria.MOEDA,
				TipoMelhoria.ESPADA, TipoMelhoria.YURT };
		int deslocamento = 0;
		for (TipoMelhoria tipo : tipos) {
			for (int i = 0; i < tipo.getQuantidade(); i++) {
				lista.add(new Melhoria(tipo, cores[(i + deslocamento) % cores.length]));
			}
			deslocamento++;
		}
		for (TipoTesouro t : TipoTesouro.values()) {
			lista.add(Melhoria.deTesouro(t));
		}
		return lista;
	}

	// ================================================================ preparacao pelos jogadores

	/** Cartas secretas oferecidas ao jogador (ele deve ficar com uma). */
	public List<String> getConselheirosSecretosOferecidos(String cor) {
		List<String> nomes = new ArrayList<String>();
		Jogador j = jogador(cor);
		if (j != null) {
			for (CartaConselheiroSecreto c : j.getSecretasOferecidas()) {
				nomes.add(c.name());
			}
		}
		return nomes;
	}

	/** Escolhe qual das 2 cartas secretas o jogador mantem, antes da primeira jogada. */
	public boolean escolherConselheiroSecreto(String cor, int indice) {
		Jogador j = jogador(cor);
		if (partidaIniciada || terminado || j == null || j.getSecretaEscolhida() != null
				|| indice < 0 || indice >= j.getSecretasOferecidas().size()) {
			return false;
		}
		j.setSecretaEscolhida(j.getSecretasOferecidas().get(indice));
		return true;
	}

	/**
	 * Opcoes iniciais de Jochi, na ordem dos indices usados para escolher.
	 * Cada mapa contem tipo, cor e tesouro (vazio se nao for melhoria de tesouro).
	 * A lista e os mapas sao copias; altera-los nao modifica a partida.
	 */
	public List<Map<String, String>> getMelhoriasIniciaisOferecidas(String cor) {
		List<Map<String, String>> opcoes = new ArrayList<Map<String, String>>();
		Jogador j = jogador(cor);
		if (j != null) {
			for (Melhoria m : j.getMelhoriasIniciaisOferecidas()) {
				Map<String, String> dados = new HashMap<String, String>();
				dados.put("tipo", m.getTipo().name());
				dados.put("cor", m.getCor().name());
				dados.put("tesouro", m.getTesouro() == null ? "" : m.getTesouro().name());
				opcoes.add(dados);
			}
		}
		return opcoes;
	}

	/** Habilidade de Jochi: escolhe qual das 2 melhorias sorteadas vai para a coluna branca. */
	public boolean escolherMelhoriaInicial(String cor, int indice) {
		Jogador j = jogador(cor);
		if (partidaIniciada || terminado || j == null || j.getTabuleiro().brancaOcupada(0)
				|| indice < 0 || indice >= j.getMelhoriasIniciaisOferecidas().size()) {
			return false;
		}
		List<Melhoria> oferecidas = j.getMelhoriasIniciaisOferecidas();
		Melhoria escolhida = oferecidas.get(indice);
		escolhida.setTurnoAquisicao(0);
		j.getTabuleiro().colocarNaBranca(0, escolhida);
		for (Melhoria m : oferecidas) {
			if (m != escolhida) {
				areaMelhorias.devolverAPilha(m);
			}
		}
		oferecidas.clear();
		return true;
	}

	/** Todas as escolhas iniciais devem ser resolvidas antes de qualquer jogada. */
	public boolean isPreparacaoConcluida() {
		for (Jogador j : jogadores) {
			if (j.getSecretaEscolhida() == null || !j.getMelhoriasIniciaisOferecidas().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	// ================================================================ jogada

	/** Cor do jogador da vez. */
	public String getJogadorDaVez() {
		return jogadorDaVez().getCor().name();
	}

	/**
	 * Coloca a peca de ativacao numa coluna (VERMELHA, VERDE, AMARELA ou BRANCA)
	 * e libera as acoes dela para este turno. Uma por turno.
	 */
	public boolean ativarColuna(String coluna) {
		CorColuna c = CorColuna.converter(coluna);
		Jogador j = jogadorDaVez();
		if (terminado || !isPreparacaoConcluida() || c == null || turno.colunaAtivada()
				|| !j.getTabuleiro().estaDisponivel(c)) {
			return false;
		}
		partidaIniciada = true;
		j.getTabuleiro().marcarUsada(c);
		turno.ativar(c, j.getTabuleiro().acoes(c, numeroTurno), j.getTabuleiro().tesourosVirtuais(c, numeroTurno));
		verificarCartasAbertas();
		return true;
	}

	/** Habilidade de Altani: escolhe o peao antes de sua primeira acao no turno. */
	public boolean escolherPeao(int indice) {
		Jogador j = jogadorDaVez();
		if (terminado || turno.peaoJaAgiu() || indice < 0 || indice >= j.getPosicoes().size()) {
			return false;
		}
		j.setPeaoAtivo(indice);
		turno.reiniciarParadasVisitadas(j.getPosicao());
		return true;
	}

	/** Informa se o peao da vez pode terminar um movimento na parada indicada. */
	public boolean podeMoverPara(String parada) {
		return calcularMovimento(parada) != null;
	}

	/**
	 * Move o peao da vez ate a parada, pelo caminho mais barato, gastando os
	 * movimentos (cavalos) necessarios. Paradas puladas sobre yurts proprios
	 * nao custam movimento e nao contam como visitadas.
	 */
	public boolean moverPara(String parada) {
		Caminho c = calcularMovimento(parada);
		if (c == null) {
			return false;
		}
		turno.gastarMovimentos(c.getCusto());
		partidaIniciada = true;
		jogadorDaVez().setPosicao(parada);
		turno.registrarMovimento(c.getParadasVisitadas());
		return true;
	}

	private Caminho calcularMovimento(String destino) {
		Jogador j = jogadorDaVez();
		Parada p = mapa.getParada(destino);
		if (terminado || !isPreparacaoConcluida() || p == null || turno.getMovimentos() == 0) {
			return null;
		}
		if (peoesNaParada(destino) >= p.getTipo().getCapacidadePeoes()) {
			return null;
		}
		Set<String> pulaveis = new HashSet<String>();
		for (Parada q : mapa.getParadas()) {
			if (q.temYurtDe(j.getCor())) {
				pulaveis.add(q.getId());
			}
		}
		Caminho c = mapa.caminhoMaisBarato(j.getPosicao(), destino, pulaveis);
		if (c == null || c.getCusto() > turno.getMovimentos()) {
			return null;
		}
		return c;
	}

	private int peoesNaParada(String parada) {
		int n = 0;
		for (Jogador j : jogadores) {
			for (String pos : j.getPosicoes()) {
				if (pos.equals(parada)) {
					n++;
				}
			}
		}
		return n;
	}

	/**
	 * Pega 1 peca de tributo de uma provincia adjacente a alguma parada em que
	 * o peao esteve neste turno. Gasta uma acao de punho.
	 */
	public boolean pegarTributo(String provincia) {
		Provincia p = mapa.getProvincia(provincia);
		if (terminado || !isPreparacaoConcluida() || p == null || turno.getPunhos() == 0
				|| p.getPecas() == 0 || !adjacenteAVisitada(p)) {
			return false;
		}
		turno.gastarPunho();
		partidaIniciada = true;
		turno.registrarAcaoDoPeao();
		p.retirarPeca();
		jogadorDaVez().adicionarTributo(p.getTipo(), 1);
		verificarCartasAbertas();
		return true;
	}

	private boolean adjacenteAVisitada(Provincia p) {
		for (String parada : mapa.paradasAdjacentesAProvincia(p.getId())) {
			if (turno.getParadasVisitadas().contains(parada)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Acao obrigatoria quando a coluna ativada tem o icone do Khan: move o Khan
	 * para uma provincia do Khan ou para a area de melhorias (AREA_MELHORIAS).
	 * O Khan nao pode permanecer onde esta.
	 */
	public boolean moverKhan(String destino) {
		if (terminado || !isPreparacaoConcluida() || !turno.isKhanPendente()
				|| destino == null || destino.equals(posicaoKhan)) {
			return false;
		}
		if (AREA_MELHORIAS.equals(destino)) {
			Jogador j = jogadorDaVez();
			if (areaMelhorias.espacoVazio(AreaMelhorias.ESPACO_MOEDA)) {
				j.adicionarTributo(TipoTributo.MOEDA, retirarDoEstoque(TipoTributo.MOEDA, 1));
			}
			if (areaMelhorias.espacoVazio(AreaMelhorias.ESPACO_ESPADA)) {
				j.adicionarTributo(TipoTributo.ESPADA, retirarDoEstoque(TipoTributo.ESPADA, 1));
			}
			areaMelhorias.repor();
		} else {
			Provincia p = mapa.getProvincia(destino);
			if (p == null || !p.isDoKhan()) {
				return false;
			}
			abastecer(p);
			for (String alvo : p.getSetasKhan()) {
				abastecer(mapa.getProvincia(alvo));
			}
		}
		posicaoKhan = destino;
		partidaIniciada = true;
		turno.khanResolvido();
		verificarCartasAbertas();
		return true;
	}

	private void abastecer(Provincia p) {
		if (!p.estaCheia() && retirarDoEstoque(p.getTipo(), 1) == 1) {
			p.adicionarPeca();
		}
	}

	// ================================================================ acoes extras

	/**
	 * Ataca uma cidade revelada adjacente a uma parada visitada no turno e toma
	 * o tesouro indicado. O 1o tesouro de uma cidade no turno custa 1 espada;
	 * cada um dos seguintes da mesma cidade custa 2.
	 */
	public boolean atacarCidade(String cidade, String tesouro) {
		Cidade c = mapa.getCidade(cidade);
		TipoTesouro t = TipoTesouro.converter(tesouro);
		if (terminado || !isPreparacaoConcluida() || c == null || t == null
				|| !c.podeSerAtacada() || !c.getTesouros().contains(t)) {
			return false;
		}
		if (!cidadeAdjacenteAVisitada(cidade)) {
			return false;
		}
		int custo = turno.getTesourosTomados(cidade) == 0 ? 1 : 2;
		if (espadasDisponiveis() < custo) {
			return false;
		}
		pagarEspadas(custo);
		partidaIniciada = true;
		turno.registrarAcaoDoPeao();
		turno.registrarTesouroTomado(cidade);
		tomarTesouro(c, t);
		return true;
	}

	private boolean cidadeAdjacenteAVisitada(String cidade) {
		for (String parada : mapa.paradasAdjacentesACidade(cidade)) {
			if (turno.getParadasVisitadas().contains(parada)) {
				return true;
			}
		}
		return false;
	}

	private void tomarTesouro(Cidade c, TipoTesouro t) {
		Jogador j = jogadorDaVez();
		c.getTesouros().remove(t);
		j.adicionarTesouro(t);
		if (c.getTesouros().isEmpty()) {
			boolean temYurt = j.getYurtsDisponiveis() > 0;
			if (temYurt) {
				j.usarYurt();
			}
			c.conquistar(j.getCor(), temYurt);
			j.getCidadesConquistadas().add(c.getNome());
			revelarProximaCidade();
			if (todasCidadesConquistadas()) {
				dispararFim();
			}
		}
		verificarCartasAbertas();
	}

	private void revelarProximaCidade() {
		if (pilhaCidades.isEmpty()) {
			return;
		}
		Cidade c = mapa.getCidade(pilhaCidades.remove(0));
		c.revelar();
		for (int i = 0; i < Cidade.TESOUROS_AO_REVELAR && !pilhaTesouros.isEmpty(); i++) {
			c.getTesouros().add(pilhaTesouros.remove(0));
		}
	}

	private boolean todasCidadesConquistadas() {
		for (Cidade c : mapa.getCidades()) {
			if (!c.isConquistada()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Constroi um yurt numa parada em que o peao esteve neste turno (Tolui
	 * tambem pode construir nas paradas adjacentes a ele). Gasta 1 tributo de
	 * yurt (ou yurt virtual).
	 */
	public boolean construirYurt(String parada) {
		Jogador j = jogadorDaVez();
		Parada p = mapa.getParada(parada);
		if (terminado || !isPreparacaoConcluida() || p == null || !p.cabeYurt() || j.getYurtsDisponiveis() == 0) {
			return false;
		}
		boolean visitada = turno.getParadasVisitadas().contains(parada);
		boolean adjacenteTolui = j.getHerdeiro() == Herdeiro.TOLUI && mapa.vizinhas(j.getPosicao()).contains(parada);
		if (!visitada && !adjacenteTolui) {
			return false;
		}
		if (turno.getYurtsVirtuais() + j.getTributos(TipoTributo.YURT) == 0) {
			return false;
		}
		if (turno.usarYurtsVirtuais(1) == 0) {
			gastarTributo(j, TipoTributo.YURT, 1);
		}
		j.usarYurt();
		p.adicionarYurt(j.getCor());
		partidaIniciada = true;
		turno.registrarAcaoDoPeao();
		verificarCartasAbertas();
		return true;
	}

	/**
	 * Compra uma melhoria por 2 moedas e a coloca numa coluna colorida da mesma
	 * cor (Jochi ignora a cor).
	 *
	 * @param espaco indice (0 a 4) na area de melhorias, ou -1 para o topo da pilha
	 */
	public boolean comprarMelhoria(int espaco, String coluna) {
		Jogador j = jogadorDaVez();
		CorColuna c = CorColuna.converter(coluna);
		if (terminado || !isPreparacaoConcluida() || c == null
				|| !j.getTabuleiro().podeColocar(c) || moedasDisponiveis() < 2) {
			return false;
		}
		Melhoria m = melhoriaEm(espaco);
		if (m == null || (j.getHerdeiro() != Herdeiro.JOCHI && m.getCor() != c)) {
			return false;
		}
		retirarMelhoria(espaco);
		partidaIniciada = true;
		pagarMoedas(2);
		m.setTurnoAquisicao(numeroTurno);
		int linha = j.getTabuleiro().colocar(c, m);
		if (j.getTabuleiro().linhaCompleta(linha) && !j.getTabuleiro().brancaOcupada(linha)
				&& areaMelhorias.temMelhoriaDisponivel()) {
			turno.getLinhasGratisPendentes().add(linha);
		}
		verificarCartasAbertas();
		return true;
	}

	/** Quantas melhorias gratuitas (por linha completada) aguardam escolha. */
	public int getMelhoriasGratisPendentes() {
		return turno.getLinhasGratisPendentes().size();
	}

	/**
	 * Recebe a melhoria gratuita por completar uma linha. Ela vai para o espaco
	 * da coluna branca na mesma linha, sem restricao de cor.
	 *
	 * @param espaco indice na area de melhorias, ou -1 para o topo da pilha
	 */
	public boolean pegarMelhoriaGratis(int espaco) {
		if (terminado || !isPreparacaoConcluida() || turno.getLinhasGratisPendentes().isEmpty()) {
			return false;
		}
		Melhoria m = melhoriaEm(espaco);
		if (m == null) {
			return false;
		}
		int linha = turno.getLinhasGratisPendentes().remove(0);
		retirarMelhoria(espaco);
		partidaIniciada = true;
		m.setTurnoAquisicao(numeroTurno);
		jogadorDaVez().getTabuleiro().colocarNaBranca(linha, m);
		verificarCartasAbertas();
		return true;
	}

	private Melhoria melhoriaEm(int espaco) {
		if (espaco == -1) {
			return areaMelhorias.topoDaPilha();
		}
		if (espaco < 0 || espaco >= AreaMelhorias.TAMANHO) {
			return null;
		}
		return areaMelhorias.getEspaco(espaco);
	}

	private void retirarMelhoria(int espaco) {
		if (espaco == -1) {
			areaMelhorias.retirarDaPilha();
		} else {
			areaMelhorias.retirar(espaco);
		}
		// O manual nao detalha o esgotamento: sem pecas, beneficios impossiveis
		// deixam de ser obrigatorios para nao impedir o encerramento do turno.
		if (!areaMelhorias.temMelhoriaDisponivel()) {
			turno.getLinhasGratisPendentes().clear();
		}
	}

	/**
	 * Entrega tesouros ao conselheiro de uma parada em que o peao esteve no
	 * turno. A entrega tem de corresponder exatamente ao que ele pede.
	 */
	public boolean entregarTesouros(String parada, List<String> tesouros) {
		Conselheiro c = mapa.getConselheiro(parada);
		if (terminado || !isPreparacaoConcluida() || c == null || tesouros == null
				|| !turno.getParadasVisitadas().contains(parada)) {
			return false;
		}
		List<TipoTesouro> entrega = new ArrayList<TipoTesouro>();
		HashMap<TipoTesouro, Integer> necessarios = new HashMap<TipoTesouro, Integer>();
		for (String s : tesouros) {
			TipoTesouro t = TipoTesouro.converter(s);
			if (t == null) {
				return false;
			}
			entrega.add(t);
			Integer n = necessarios.get(t);
			necessarios.put(t, n == null ? 1 : n + 1);
		}
		if (!c.aceita(entrega)) {
			return false;
		}
		Jogador j = jogadorDaVez();
		for (TipoTesouro t : necessarios.keySet()) {
			if (j.getTesouros(t) + turno.getTesourosVirtuais(t) < necessarios.get(t)) {
				return false;
			}
		}
		partidaIniciada = true;
		turno.registrarAcaoDoPeao();
		for (TipoTesouro t : entrega) {
			if (!turno.usarTesouroVirtual(t)) {
				j.removerTesouro(t);
			}
		}
		ganharVotos(j, c.getVotos());
		PecaBonus b = c.retirarBonus();
		if (b != null) {
			j.receberBonus(b);
			if (b.isImediato()) {
				ganharVotos(j, 1);
			}
		}
		verificarCartasAbertas();
		return true;
	}

	/** Usa uma peca de bonus guardada pelo jogador da vez. */
	public boolean usarBonus(String bonus) {
		Jogador j = jogadorDaVez();
		PecaBonus b = PecaBonus.converter(bonus);
		if (terminado || !isPreparacaoConcluida() || b == null || !j.getBonusDisponiveis().contains(b)
				|| !turno.getTesourosDoBonus().isEmpty()) {
			return false;
		}
		switch (b) {
		case DUAS_ESPADAS:
			j.adicionarTributo(TipoTributo.ESPADA, retirarDoEstoque(TipoTributo.ESPADA, 2));
			break;
		case DUAS_MOEDAS:
			j.adicionarTributo(TipoTributo.MOEDA, retirarDoEstoque(TipoTributo.MOEDA, 2));
			break;
		case DOIS_YURTS:
			j.adicionarTributo(TipoTributo.YURT, retirarDoEstoque(TipoTributo.YURT, 2));
			break;
		case TRES_CAVALOS_UM_PUNHO:
			turno.adicionarMovimentos(3);
			turno.adicionarPunhos(1);
			break;
		case DOIS_PUNHOS:
			turno.adicionarPunhos(2);
			break;
		case TRES_TESOUROS:
			for (int i = 0; i < 3 && !pilhaTesouros.isEmpty(); i++) {
				turno.getTesourosDoBonus().add(pilhaTesouros.remove(0));
			}
			break;
		default:
			return false;
		}
		j.getBonusDisponiveis().remove(b);
		partidaIniciada = true;
		verificarCartasAbertas();
		return true;
	}

	/** Tesouros sorteados pelo bonus de 3 tesouros, aguardando a escolha. */
	public List<String> getTesourosDoBonus() {
		List<String> nomes = new ArrayList<String>();
		for (TipoTesouro t : turno.getTesourosDoBonus()) {
			nomes.add(t.name());
		}
		return nomes;
	}

	/** Escolhe 1 dos 3 tesouros do bonus; os outros sao descartados. */
	public boolean escolherTesouroDoBonus(String tesouro) {
		TipoTesouro t = TipoTesouro.converter(tesouro);
		if (terminado || !isPreparacaoConcluida() || t == null || !turno.getTesourosDoBonus().contains(t)) {
			return false;
		}
		jogadorDaVez().adicionarTesouro(t);
		partidaIniciada = true;
		turno.getTesourosDoBonus().clear();
		verificarCartasAbertas();
		return true;
	}

	// ================================================================ habilidades

	/** Chagatai: 1x por turno, deixa de usar 1 cavalo para ganhar 1 espada. */
	public boolean trocarCavaloPorEspada() {
		Jogador j = jogadorDaVez();
		if (terminado || !isPreparacaoConcluida() || j.getHerdeiro() != Herdeiro.CHAGATAI || turno.isHabilidadeUsada()
				|| turno.getMovimentos() == 0 || estoqueTributos.get(TipoTributo.ESPADA) == 0) {
			return false;
		}
		turno.gastarMovimentos(1);
		partidaIniciada = true;
		turno.usarHabilidade();
		j.adicionarTributo(TipoTributo.ESPADA, retirarDoEstoque(TipoTributo.ESPADA, 1));
		verificarCartasAbertas();
		return true;
	}

	/**
	 * Ogedei: 1x por turno, paga 1 moeda para pegar um tesouro de qualquer
	 * cidade do mapa, desde que nao seja o ultimo tesouro dela.
	 */
	public boolean comprarTesouroComMoeda(String cidade, String tesouro) {
		Jogador j = jogadorDaVez();
		Cidade c = mapa.getCidade(cidade);
		TipoTesouro t = TipoTesouro.converter(tesouro);
		if (terminado || !isPreparacaoConcluida() || j.getHerdeiro() != Herdeiro.OGEDEI
				|| turno.isHabilidadeUsada() || c == null || t == null) {
			return false;
		}
		if (!c.podeSerAtacada() || c.getTesouros().size() < 2 || !c.getTesouros().contains(t) || moedasDisponiveis() < 1) {
			return false;
		}
		pagarMoedas(1);
		partidaIniciada = true;
		turno.usarHabilidade();
		tomarTesouro(c, t);
		return true;
	}

	// ================================================================ fim do turno

	/**
	 * Encerra o turno do jogador da vez. So e permitido depois de ativar uma
	 * coluna e resolver o que for obrigatorio (Khan, melhoria gratis, bonus).
	 */
	public boolean finalizarTurno() {
		if (terminado || !turno.podeTerminar()) {
			return false;
		}
		Jogador j = jogadorDaVez();
		if (turno.isRecuperar()) {
			j.getTabuleiro().recuperarTodas();
		}
		if (fimDisparado) {
			if (turnosRestantes == 0) {
				encerrarPartida();
				return true;
			}
			turnosRestantes--;
		}
		indiceDaVez = (indiceDaVez + 1) % jogadores.size();
		numeroTurno++;
		turno = new Turno(numeroTurno, jogadorDaVez().getPosicao());
		return true;
	}

	/**
	 * Fim disparado: termina-se a rodada atual e joga-se mais uma rodada
	 * completa.
	 */
	private void dispararFim() {
		if (fimDisparado) {
			return;
		}
		fimDisparado = true;
		int n = jogadores.size();
		turnosRestantes = (n - 1 - indiceDaVez) + n;
	}

	private void ganharVotos(Jogador j, int votos) {
		j.adicionarVotos(votos);
		trilha.avancar(votos);
		if (trilha.atingiuLimite()) {
			dispararFim();
		}
	}

	private void verificarCartasAbertas() {
		Jogador j = jogadorDaVez();
		for (CartaConselheiroAberto carta : new ArrayList<CartaConselheiroAberto>(cartasAbertas)) {
			if (carta.cumprida(j, mapa, turno)) {
				cartasAbertas.remove(carta);
				ganharVotos(j, 1);
			}
		}
	}

	private void encerrarPartida() {
		terminado = true;
		pontuarInfluencia();
		for (Jogador j : jogadores) {
			CartaConselheiroSecreto carta = j.getSecretaValida();
			if (carta != null) {
				j.adicionarVotos(carta.votos(j, mapa));
			}
		}
	}

	/**
	 * Votos de influencia em cada regiao (tabela da pagina 15 do manual),
	 * indexados pelo numero de melhorias (0 a 4) na coluna da regiao.
	 */
	private static final int[] VOTOS_PRIMEIRO = { 1, 1, 2, 2, 3 };
	private static final int[] VOTOS_SEGUNDO = { 0, 1, 1, 2, 2 };

	void pontuarInfluencia() {
		for (Regiao r : Regiao.values()) {
			CorColuna coluna = CorColuna.daRegiao(r);
			HashMap<Jogador, Integer> yurts = new HashMap<Jogador, Integer>();
			int maior = 0;
			for (Jogador j : jogadores) {
				int n = mapa.yurtsNaRegiao(j.getCor(), r);
				yurts.put(j, n);
				maior = Math.max(maior, n);
			}
			if (maior == 0) {
				continue;
			}
			List<Jogador> primeiros = new ArrayList<Jogador>();
			int segundo = 0;
			for (Jogador j : jogadores) {
				if (yurts.get(j) == maior) {
					primeiros.add(j);
				} else {
					segundo = Math.max(segundo, yurts.get(j));
				}
			}
			for (Jogador j : primeiros) {
				j.adicionarVotos(VOTOS_PRIMEIRO[j.getTabuleiro().quantidade(coluna)]);
			}
			// empate no 1o lugar: ninguem fica em 2o
			if (primeiros.size() == 1 && segundo > 0) {
				for (Jogador j : jogadores) {
					if (yurts.get(j) == segundo) {
						j.adicionarVotos(VOTOS_SEGUNDO[j.getTabuleiro().quantidade(coluna)]);
					}
				}
			}
		}
	}

	/**
	 * Vencedores da partida (mais de um em caso de empate persistente).
	 * Desempate: mais tesouros nao entregues; depois, peao mais proximo de
	 * Karakorum.
	 */
	public List<String> getVencedores() {
		if (!terminado) {
			return new ArrayList<String>();
		}
		return calcularVencedores();
	}

	List<String> calcularVencedores() {
		List<String> vencedores = new ArrayList<String>();
		List<Jogador> melhores = new ArrayList<Jogador>();
		for (Jogador j : jogadores) {
			if (melhores.isEmpty()) {
				melhores.add(j);
				continue;
			}
			int cmp = comparar(j, melhores.get(0));
			if (cmp > 0) {
				melhores.clear();
				melhores.add(j);
			} else if (cmp == 0) {
				melhores.add(j);
			}
		}
		for (Jogador j : melhores) {
			vencedores.add(j.getCor().name());
		}
		return vencedores;
	}

	private int comparar(Jogador a, Jogador b) {
		if (a.getVotos() != b.getVotos()) {
			return a.getVotos() - b.getVotos();
		}
		if (a.totalTesouros() != b.totalTesouros()) {
			return a.totalTesouros() - b.totalTesouros();
		}
		return distanciaAKarakorum(b) - distanciaAKarakorum(a);
	}

	private int distanciaAKarakorum(Jogador j) {
		int menor = Integer.MAX_VALUE;
		for (String pos : j.getPosicoes()) {
			menor = Math.min(menor, mapa.distancia(pos, Mapa.KARAKORUM));
		}
		return menor;
	}

	// ================================================================ recursos

	private int retirarDoEstoque(TipoTributo t, int quantidade) {
		int retirada = Math.min(quantidade, estoqueTributos.get(t));
		estoqueTributos.put(t, estoqueTributos.get(t) - retirada);
		return retirada;
	}

	/** Tributo usado volta para o estoque. */
	private void gastarTributo(Jogador j, TipoTributo t, int quantidade) {
		j.removerTributo(t, quantidade);
		estoqueTributos.put(t, estoqueTributos.get(t) + quantidade);
	}

	private int moedasDisponiveis() {
		return turno.getMoedasVirtuais() + jogadorDaVez().getTributos(TipoTributo.MOEDA);
	}

	private void pagarMoedas(int n) {
		int restante = n - turno.usarMoedasVirtuais(n);
		if (restante > 0) {
			gastarTributo(jogadorDaVez(), TipoTributo.MOEDA, restante);
		}
	}

	private int espadasDisponiveis() {
		return turno.getEspadasVirtuais() + jogadorDaVez().getTributos(TipoTributo.ESPADA);
	}

	private void pagarEspadas(int n) {
		int restante = n - turno.usarEspadasVirtuais(n);
		if (restante > 0) {
			gastarTributo(jogadorDaVez(), TipoTributo.ESPADA, restante);
		}
	}

	// ================================================================ consultas publicas

	public int getNumeroJogadores() {
		return jogadores.size();
	}

	public String getHerdeiro(String cor) {
		Jogador j = jogador(cor);
		return j == null ? null : j.getHerdeiro().name();
	}

	public int getVotos(String cor) {
		Jogador j = jogador(cor);
		return j == null ? 0 : j.getVotos();
	}

	public int getTributos(String cor, String tipo) {
		Jogador j = jogador(cor);
		TipoTributo t = TipoTributo.converter(tipo);
		return (j == null || t == null) ? 0 : j.getTributos(t);
	}

	public int getTesouros(String cor, String tipo) {
		Jogador j = jogador(cor);
		TipoTesouro t = TipoTesouro.converter(tipo);
		return (j == null || t == null) ? 0 : j.getTesouros(t);
	}

	public int getYurtsDisponiveis(String cor) {
		Jogador j = jogador(cor);
		return j == null ? 0 : j.getYurtsDisponiveis();
	}

	/** Posicao do peao ativo do jogador. */
	public String getPosicao(String cor) {
		Jogador j = jogador(cor);
		return j == null ? null : j.getPosicao();
	}

	public int getMovimentosRestantes() {
		return turno.getMovimentos();
	}

	public int getPunhosRestantes() {
		return turno.getPunhos();
	}

	public boolean isKhanPendente() {
		return turno.isKhanPendente();
	}

	/** Onde esta o Khan (null enquanto estiver fora do tabuleiro). */
	public String getPosicaoKhan() {
		return posicaoKhan;
	}

	public int getPecasNaProvincia(String provincia) {
		Provincia p = mapa.getProvincia(provincia);
		return p == null ? 0 : p.getPecas();
	}

	public List<String> getCidadesReveladas() {
		List<String> nomes = new ArrayList<String>();
		for (Cidade c : mapa.getCidades()) {
			if (c.podeSerAtacada()) {
				nomes.add(c.getNome());
			}
		}
		Collections.sort(nomes);
		return nomes;
	}

	public List<String> getTesourosDaCidade(String cidade) {
		List<String> nomes = new ArrayList<String>();
		Cidade c = mapa.getCidade(cidade);
		if (c != null) {
			for (TipoTesouro t : c.getTesouros()) {
				nomes.add(t.name());
			}
		}
		return nomes;
	}

	public List<String> getCartasAbertas() {
		List<String> nomes = new ArrayList<String>();
		for (CartaConselheiroAberto c : cartasAbertas) {
			nomes.add(c.name());
		}
		return nomes;
	}

	public boolean isFimDisparado() {
		return fimDisparado;
	}

	public boolean isTerminado() {
		return terminado;
	}

	// ================================================================ acesso interno (testes)

	Jogador jogador(String cor) {
		return jogadoresPorCor.get(CorJogador.converter(cor));
	}

	Jogador jogadorDaVez() {
		return jogadores.get(indiceDaVez);
	}

	Mapa getMapa() {
		return mapa;
	}

	Turno getTurno() {
		return turno;
	}

	AreaMelhorias getAreaMelhorias() {
		return areaMelhorias;
	}

	TrilhaKurultai getTrilha() {
		return trilha;
	}

	List<CartaConselheiroAberto> getCartasAbertasInternas() {
		return cartasAbertas;
	}

	int getEstoque(TipoTributo t) {
		return estoqueTributos.get(t);
	}
}
