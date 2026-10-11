package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Estado do turno corrente: coluna ativada, acoes restantes, recursos
 * virtuais (que so valem neste turno) e paradas em que o peao esteve.
 */
class Turno {
	private final int numero;
	private CorColuna coluna;
	private int movimentos;
	private int punhos;
	private int moedasVirtuais;
	private int espadasVirtuais;
	private int yurtsVirtuais;
	private final HashMap<TipoTesouro, Integer> tesourosVirtuais = new HashMap<TipoTesouro, Integer>();
	private final Set<String> paradasVisitadas = new HashSet<String>();
	private final HashMap<String, Integer> tesourosTomados = new HashMap<String, Integer>();
	private boolean khanPendente;
	private boolean recuperar;
	private boolean habilidadeUsada;
	private boolean moveu;
	private boolean peaoAgiu;
	private final List<Integer> linhasGratisPendentes = new ArrayList<Integer>();
	private final List<TipoTesouro> tesourosDoBonus = new ArrayList<TipoTesouro>();

	Turno(int numero, String posicaoInicial) {
		this.numero = numero;
		paradasVisitadas.add(posicaoInicial);
		for (TipoTesouro t : TipoTesouro.values()) {
			tesourosVirtuais.put(t, 0);
		}
	}

	int getNumero() {
		return numero;
	}

	CorColuna getColuna() {
		return coluna;
	}

	boolean colunaAtivada() {
		return coluna != null;
	}

	/** Registra a coluna ativada e soma as acoes dela. */
	void ativar(CorColuna c, List<Acao> acoes, List<TipoTesouro> tesouros) {
		coluna = c;
		for (Acao a : acoes) {
			switch (a) {
			case CAVALO:
				movimentos++;
				break;
			case PUNHO:
				punhos++;
				break;
			case KHAN:
				khanPendente = true;
				break;
			case MOEDA_VIRTUAL:
				moedasVirtuais++;
				break;
			case ESPADA_VIRTUAL:
				espadasVirtuais++;
				break;
			case YURT_VIRTUAL:
				yurtsVirtuais++;
				break;
			case RECUPERAR:
				recuperar = true;
				break;
			default:
				break;
			}
		}
		for (TipoTesouro t : tesouros) {
			tesourosVirtuais.put(t, tesourosVirtuais.get(t) + 1);
		}
	}

	// ------------------------------------------------------------ movimento

	int getMovimentos() {
		return movimentos;
	}

	void adicionarMovimentos(int n) {
		movimentos += n;
	}

	void gastarMovimentos(int n) {
		if (n > movimentos) {
			throw new IllegalStateException("Movimentos insuficientes");
		}
		movimentos -= n;
	}

	boolean jaMoveu() {
		return moveu;
	}

	boolean peaoJaAgiu() {
		return peaoAgiu;
	}

	/** Fixa o peao do turno depois da primeira acao que depende de sua posicao. */
	void registrarAcaoDoPeao() {
		peaoAgiu = true;
	}

	void registrarMovimento(List<String> visitadas) {
		moveu = true;
		registrarAcaoDoPeao();
		paradasVisitadas.addAll(visitadas);
	}

	Set<String> getParadasVisitadas() {
		return paradasVisitadas;
	}

	void reiniciarParadasVisitadas(String posicao) {
		paradasVisitadas.clear();
		paradasVisitadas.add(posicao);
	}

	// ------------------------------------------------------------ punhos

	int getPunhos() {
		return punhos;
	}

	void adicionarPunhos(int n) {
		punhos += n;
	}

	void gastarPunho() {
		if (punhos == 0) {
			throw new IllegalStateException("Sem acoes de pegar tributo");
		}
		punhos--;
	}

	// ------------------------------------------------------------ virtuais

	int getMoedasVirtuais() {
		return moedasVirtuais;
	}

	int getEspadasVirtuais() {
		return espadasVirtuais;
	}

	int getYurtsVirtuais() {
		return yurtsVirtuais;
	}

	int getTesourosVirtuais(TipoTesouro t) {
		return tesourosVirtuais.get(t);
	}

	/** Usa ate n moedas virtuais e devolve quantas foram usadas. */
	int usarMoedasVirtuais(int n) {
		int usadas = Math.min(n, moedasVirtuais);
		moedasVirtuais -= usadas;
		return usadas;
	}

	int usarEspadasVirtuais(int n) {
		int usadas = Math.min(n, espadasVirtuais);
		espadasVirtuais -= usadas;
		return usadas;
	}

	int usarYurtsVirtuais(int n) {
		int usados = Math.min(n, yurtsVirtuais);
		yurtsVirtuais -= usados;
		return usados;
	}

	boolean usarTesouroVirtual(TipoTesouro t) {
		if (tesourosVirtuais.get(t) == 0) {
			return false;
		}
		tesourosVirtuais.put(t, tesourosVirtuais.get(t) - 1);
		return true;
	}

	// ------------------------------------------------------------ cidades

	int getTesourosTomados(String cidade) {
		Integer n = tesourosTomados.get(cidade);
		return n == null ? 0 : n;
	}

	void registrarTesouroTomado(String cidade) {
		tesourosTomados.put(cidade, getTesourosTomados(cidade) + 1);
	}

	// ------------------------------------------------------------ outros

	boolean isKhanPendente() {
		return khanPendente;
	}

	void khanResolvido() {
		khanPendente = false;
	}

	boolean isRecuperar() {
		return recuperar;
	}

	boolean isHabilidadeUsada() {
		return habilidadeUsada;
	}

	void usarHabilidade() {
		habilidadeUsada = true;
	}

	List<Integer> getLinhasGratisPendentes() {
		return linhasGratisPendentes;
	}

	List<TipoTesouro> getTesourosDoBonus() {
		return tesourosDoBonus;
	}

	/** O turno so pode terminar quando nao houver escolha obrigatoria pendente. */
	boolean podeTerminar() {
		return colunaAtivada() && !khanPendente && linhasGratisPendentes.isEmpty() && tesourosDoBonus.isEmpty();
	}
}
