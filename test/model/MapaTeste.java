package model;

/**
 * Mapa minimo usado nos testes de movimento:
 *
 * KARAKORUM - B - C - D(dupla), com a provincia X adjacente apenas a B.
 */
final class MapaTeste {
	private MapaTeste() {
	}

	static Mapa criar() {
		Mapa m = new Mapa();
		m.adicionarParada(Mapa.KARAKORUM, TipoParada.KARAKORUM, null);
		m.adicionarParada("B", TipoParada.SIMPLES, Regiao.RUSSIA);
		m.adicionarParada("C", TipoParada.SIMPLES, Regiao.RUSSIA);
		m.adicionarParada("D", TipoParada.DUPLA, Regiao.RUSSIA);
		m.ligar(Mapa.KARAKORUM, "B");
		m.ligar("B", "C");
		m.ligar("C", "D");
		m.adicionarProvincia("X", TipoTributo.ESPADA, false, "B");
		return m;
	}
}
