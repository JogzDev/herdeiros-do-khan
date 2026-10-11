package model;

/**
 * Topologia PROVISORIA do mapa.
 *
 * Ainda nao temos a imagem do tabuleiro principal vista de cima, entao este
 * mapa respeita a estrutura descrita no manual (3 regioes, Karakorum no
 * centro, 9 cidades, 6 paradas duplas de conselheiro, 4 provincias do Khan
 * com 2 setas cada), mas as ligacoes entre paradas sao ficticias.
 *
 * Quando a imagem do tabuleiro for disponibilizada, basta reescrever este
 * metodo: as regras em Jogo e Mapa nao dependem destes dados.
 */
class MapaProvisorio {

	private MapaProvisorio() {
	}

	static Mapa criar() {
		Mapa m = new Mapa();
		m.adicionarParada(Mapa.KARAKORUM, TipoParada.KARAKORUM, null);

		montarRegiao(m, "R", Regiao.RUSSIA);
		montarRegiao(m, "C", Regiao.CHINA);
		montarRegiao(m, "P", Regiao.PERSIA);

		// ligacoes entre regioes
		m.ligar("R3", "C2");
		m.ligar("C3", "P3");
		m.ligar("P2", "R2");

		// provincias: <regiao>V1..V4
		m.adicionarProvincia("RV1", TipoTributo.ESPADA, false, "R1", "R2", "RD1");
		m.adicionarProvincia("RV2", TipoTributo.MOEDA, true, "R2", "R3");
		m.adicionarProvincia("RV3", TipoTributo.YURT, false, "R3", "R4", "RD1");
		m.adicionarProvincia("RV4", TipoTributo.MOEDA, false, "R4", "R5", "RD2");

		m.adicionarProvincia("CV1", TipoTributo.YURT, false, "C1", "C2", "CD1");
		m.adicionarProvincia("CV2", TipoTributo.ESPADA, true, "C2", "C3");
		m.adicionarProvincia("CV3", TipoTributo.MOEDA, false, "C3", "C4", "CD1");
		m.adicionarProvincia("CV4", TipoTributo.ESPADA, false, "C4", "C5", "CD2");

		m.adicionarProvincia("PV1", TipoTributo.MOEDA, false, "P1", "P2", "PD1");
		m.adicionarProvincia("PV2", TipoTributo.YURT, true, "P2", "P3");
		m.adicionarProvincia("PV3", TipoTributo.ESPADA, false, "P3", "P4", "PD1");
		m.adicionarProvincia("PV4", TipoTributo.YURT, false, "P4", "P5", "PD2");

		// quarta provincia do Khan, junto a Karakorum
		m.adicionarProvincia("KV", TipoTributo.MOEDA, true, Mapa.KARAKORUM, "R1", "C1");

		m.definirSetasKhan("RV2", "RV1", "RV3");
		m.definirSetasKhan("CV2", "CV1", "CV3");
		m.definirSetasKhan("PV2", "PV1", "PV3");
		m.definirSetasKhan("KV", "RV1", "CV1");

		// cidades
		m.adicionarCidade("Kiev", Regiao.RUSSIA, "R1", "R2");
		m.adicionarCidade("Moscou", Regiao.RUSSIA, "R3", "R4");
		m.adicionarCidade("Sarai", Regiao.RUSSIA, "R5", "RD2");
		m.adicionarCidade("Pequim", Regiao.CHINA, "C1", "C2");
		m.adicionarCidade("Kaesong", Regiao.CHINA, "C3", "C4");
		m.adicionarCidade("Cantao", Regiao.CHINA, "C5", "CD2");
		m.adicionarCidade("Samarcanda", Regiao.PERSIA, "P1", "P2");
		m.adicionarCidade("Bagda", Regiao.PERSIA, "P3", "P4");
		m.adicionarCidade("Cabul", Regiao.PERSIA, "P5", "PD2");

		// conselheiros: 5 pedem 2 tesouros iguais, 1 pede 3 quaisquer,
		// Karakorum pede 3 especificos (peca de Karakorum) e da 2 votos
		m.adicionarConselheiro(new Conselheiro("RD1", 1, 0, TipoTesouro.PELE, TipoTesouro.PELE));
		m.adicionarConselheiro(new Conselheiro("RD2", 1, 0, TipoTesouro.BOI, TipoTesouro.BOI));
		m.adicionarConselheiro(new Conselheiro("CD1", 1, 0, TipoTesouro.SEDA, TipoTesouro.SEDA));
		m.adicionarConselheiro(new Conselheiro("CD2", 1, 0, TipoTesouro.FERRO, TipoTesouro.FERRO));
		m.adicionarConselheiro(new Conselheiro("PD1", 1, 0, TipoTesouro.ESPECIARIA, TipoTesouro.ESPECIARIA));
		m.adicionarConselheiro(new Conselheiro("PD2", 1, 3));
		m.adicionarConselheiro(new Conselheiro(Mapa.KARAKORUM, 2, 0,
				TipoTesouro.SEDA, TipoTesouro.FERRO, TipoTesouro.ESPECIARIA));
		return m;
	}

	/**
	 * Cada regiao tem 5 paradas simples (X1..X5) e 2 duplas (XD1, XD2):
	 * KARAKORUM - X1 - X2 - X3 - X4 - X5 - XD2, com X1 - XD1 - X4.
	 */
	private static void montarRegiao(Mapa m, String prefixo, Regiao regiao) {
		for (int i = 1; i <= 5; i++) {
			m.adicionarParada(prefixo + i, TipoParada.SIMPLES, regiao);
		}
		m.adicionarParada(prefixo + "D1", TipoParada.DUPLA, regiao);
		m.adicionarParada(prefixo + "D2", TipoParada.DUPLA, regiao);
		m.ligar(Mapa.KARAKORUM, prefixo + "1");
		for (int i = 1; i < 5; i++) {
			m.ligar(prefixo + i, prefixo + (i + 1));
		}
		m.ligar(prefixo + "1", prefixo + "D1");
		m.ligar(prefixo + "D1", prefixo + "4");
		m.ligar(prefixo + "5", prefixo + "D2");
	}
}
