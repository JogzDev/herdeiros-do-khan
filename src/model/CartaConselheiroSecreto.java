package model;

import java.util.HashMap;
import java.util.List;

/**
 * As 10 cartas de conselheiro secreto. No fim do jogo cada carta mede algo no
 * estado do jogador e da 1, 2 ou 3 votos conforme os limites impressos.
 */
enum CartaConselheiroSecreto {
	VIAJANTE(3, 6, 8) {
		int medir(Jogador j, Mapa m) {
			return m.maiorRedeDeYurts(j.getCor());
		}
	},
	SOLDADO(4, 7, 10) {
		int medir(Jogador j, Mapa m) {
			return j.getTabuleiro().todasMelhorias().size();
		}
	},
	COLECIONADOR(4, 6, 7) {
		int medir(Jogador j, Mapa m) {
			return contarSimbolos(j).size();
		}
	},
	SACERDOTE(1, 2, 4) {
		int medir(Jogador j, Mapa m) {
			int pares = 0;
			for (int n : contarSimbolos(j).values()) {
				pares += n / 2;
			}
			return pares;
		}
	},
	GOVERNADOR(1, 2, 3) {
		int medir(Jogador j, Mapa m) {
			int regioes = 0;
			for (Regiao r : Regiao.values()) {
				CorColuna coluna = CorColuna.daRegiao(r);
				if (m.yurtsNaRegiao(j.getCor(), r) >= 2 && j.getTabuleiro().quantidade(coluna) >= 2) {
					regioes++;
				}
			}
			return regioes;
		}
	},
	BAJULADOR(2, 3, 4) {
		int medir(Jogador j, Mapa m) {
			int provincias = 0;
			for (Provincia p : m.getProvincias()) {
				if (p.isDoKhan() && m.yurtsAdjacentesAProvincia(j.getCor(), p.getId()) >= 2) {
					provincias++;
				}
			}
			return provincias;
		}
	},
	PREFEITO(2, 3, 4) {
		int medir(Jogador j, Mapa m) {
			return cidadesComYurtsAdjacentes(j, m, 2);
		}
	},
	CIDADAO(3, 6, 8) {
		int medir(Jogador j, Mapa m) {
			return cidadesComYurtsAdjacentes(j, m, 1);
		}
	},
	CAVALEIRO(2, 4, 5) {
		int medir(Jogador j, Mapa m) {
			int n = 0;
			for (Melhoria mel : j.getTabuleiro().todasMelhorias()) {
				if (mel.temCavaloOuPunho()) {
					n++;
				}
			}
			return n;
		}
	},
	MENSAGEIRO(2, 4, 6) {
		int medir(Jogador j, Mapa m) {
			int n = 0;
			for (Parada p : m.getParadas()) {
				if (p.getTipo() == TipoParada.DUPLA && p.temYurtDe(j.getCor())) {
					n++;
				}
			}
			return n;
		}
	};

	private final int[] limites;

	CartaConselheiroSecreto(int umVoto, int doisVotos, int tresVotos) {
		this.limites = new int[] { umVoto, doisVotos, tresVotos };
	}

	abstract int medir(Jogador j, Mapa m);

	/** Votos de fim de jogo (0 a 3). */
	int votos(Jogador j, Mapa m) {
		int valor = medir(j, m);
		int votos = 0;
		for (int limite : limites) {
			if (valor >= limite) {
				votos++;
			}
		}
		return votos;
	}

	private static HashMap<String, Integer> contarSimbolos(Jogador j) {
		HashMap<String, Integer> contagem = new HashMap<String, Integer>();
		List<Melhoria> melhorias = j.getTabuleiro().todasMelhorias();
		for (Melhoria mel : melhorias) {
			Integer atual = contagem.get(mel.getSimbolo());
			contagem.put(mel.getSimbolo(), atual == null ? 1 : atual + 1);
		}
		return contagem;
	}

	private static int cidadesComYurtsAdjacentes(Jogador j, Mapa m, int minimo) {
		int n = 0;
		for (Cidade c : m.getCidades()) {
			if (m.yurtsAdjacentesACidade(j.getCor(), c.getNome()) >= minimo) {
				n++;
			}
		}
		return n;
	}
}
