package model;

import java.util.HashMap;
import java.util.List;

/**
 * Conselheiro impresso no mapa (6 paradas duplas + Karakorum). Pede um
 * conjunto de tesouros e da votos por entrega. A peca de bonus vai para quem
 * fizer a primeira entrega.
 */
class Conselheiro {
	private final String paradaId;
	private final HashMap<TipoTesouro, Integer> demanda = new HashMap<TipoTesouro, Integer>();
	private final int quantidadeQualquer;
	private final int votos;
	private PecaBonus bonus;

	/**
	 * @param quantidadeQualquer se maior que zero, o conselheiro aceita essa
	 *                           quantidade de tesouros de qualquer tipo e a
	 *                           demanda especifica e ignorada
	 */
	Conselheiro(String paradaId, int votos, int quantidadeQualquer, TipoTesouro... demandaEspecifica) {
		this.paradaId = paradaId;
		this.votos = votos;
		this.quantidadeQualquer = quantidadeQualquer;
		for (TipoTesouro t : demandaEspecifica) {
			Integer atual = demanda.get(t);
			demanda.put(t, atual == null ? 1 : atual + 1);
		}
	}

	String getParadaId() {
		return paradaId;
	}

	int getVotos() {
		return votos;
	}

	HashMap<TipoTesouro, Integer> getDemanda() {
		return demanda;
	}

	int getQuantidadeQualquer() {
		return quantidadeQualquer;
	}

	PecaBonus getBonus() {
		return bonus;
	}

	void setBonus(PecaBonus bonus) {
		this.bonus = bonus;
	}

	PecaBonus retirarBonus() {
		PecaBonus b = bonus;
		bonus = null;
		return b;
	}

	/** Verifica se a entrega corresponde exatamente ao que o conselheiro pede. */
	boolean aceita(List<TipoTesouro> entrega) {
		if (quantidadeQualquer > 0) {
			return entrega.size() == quantidadeQualquer;
		}
		HashMap<TipoTesouro, Integer> contagem = new HashMap<TipoTesouro, Integer>();
		for (TipoTesouro t : entrega) {
			Integer atual = contagem.get(t);
			contagem.put(t, atual == null ? 1 : atual + 1);
		}
		return contagem.equals(demanda);
	}
}
