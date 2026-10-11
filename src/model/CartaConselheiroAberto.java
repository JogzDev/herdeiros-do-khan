package model;

/**
 * As 14 cartas de conselheiro aberto. Quatro sao reveladas por partida e o
 * primeiro jogador a cumprir a condicao de uma carta ganha 1 voto.
 * Cada constante implementa a sua propria condicao.
 */
enum CartaConselheiroAberto {
	QUATRO_ESPADAS {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return j.getTributos(TipoTributo.ESPADA) + (t == null ? 0 : t.getEspadasVirtuais()) >= 4;
		}
	},
	SEIS_YURTS {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return j.getYurtsNoMapa() >= 6;
		}
	},
	DEZ_YURTS {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return j.getYurtsNoMapa() >= 10;
		}
	},
	QUATRO_TESOUROS {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			int total = 0;
			for (TipoTesouro tipo : TipoTesouro.values()) {
				total += tesouros(j, t, tipo);
			}
			return total >= 4;
		}
	},
	DOIS_YURTS_POR_REGIAO {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return minimoPorRegiao(j, m) >= 2;
		}
	},
	UM_YURT_POR_REGIAO {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return minimoPorRegiao(j, m) >= 1;
		}
	},
	DOIS_BONUS {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return j.getBonusObtidos() >= 2;
		}
	},
	TRES_BONUS {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return j.getBonusObtidos() >= 3;
		}
	},
	CINCO_TESOUROS_DIFERENTES {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			for (TipoTesouro tipo : TipoTesouro.values()) {
				if (tesouros(j, t, tipo) == 0) {
					return false;
				}
			}
			return true;
		}
	},
	TRES_CIDADES {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return j.getCidadesConquistadas().size() >= 3;
		}
	},
	DUAS_CIDADES {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			return j.getCidadesConquistadas().size() >= 2;
		}
	},
	LINHA_DE_MELHORIAS {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			for (int linha = 0; linha < TabuleiroJogador.LINHAS; linha++) {
				if (j.getTabuleiro().linhaCompleta(linha)) {
					return true;
				}
			}
			return false;
		}
	},
	QUATRO_MELHORIAS_NUMA_COLUNA {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			for (CorColuna c : CorColuna.values()) {
				if (j.getTabuleiro().quantidade(c) >= 4) {
					return true;
				}
			}
			return false;
		}
	},
	DUAS_COLUNAS_COM_DUAS {
		boolean cumprida(Jogador j, Mapa m, Turno t) {
			int colunas = 0;
			for (CorColuna c : CorColuna.values()) {
				if (j.getTabuleiro().quantidade(c) >= 2) {
					colunas++;
				}
			}
			return colunas >= 2;
		}
	};

	static final int CARTAS_POR_PARTIDA = 4;


	/**
	 * @param t turno corrente do jogador (recursos virtuais contam), ou null
	 */
	abstract boolean cumprida(Jogador j, Mapa m, Turno t);

	private static int tesouros(Jogador j, Turno t, TipoTesouro tipo) {
		return j.getTesouros(tipo) + (t == null ? 0 : t.getTesourosVirtuais(tipo));
	}

	private static int minimoPorRegiao(Jogador j, Mapa m) {
		int minimo = Integer.MAX_VALUE;
		for (Regiao r : Regiao.values()) {
			minimo = Math.min(minimo, m.yurtsNaRegiao(j.getCor(), r));
		}
		return minimo;
	}
}
