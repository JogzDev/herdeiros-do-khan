package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Provincia: area entre rotas que acumula pecas de tributo de um unico tipo
 * (no maximo 3). As provincias do Khan tem setas para 2 outras provincias.
 */
class Provincia {
	static final int MAXIMO_PECAS = 3;

	private final String id;
	private final TipoTributo tipo;
	private final boolean doKhan;
	private final List<String> setasKhan = new ArrayList<String>();
	private int pecas;

	Provincia(String id, TipoTributo tipo, boolean doKhan) {
		this.id = id;
		this.tipo = tipo;
		this.doKhan = doKhan;
	}

	String getId() {
		return id;
	}

	TipoTributo getTipo() {
		return tipo;
	}

	boolean isDoKhan() {
		return doKhan;
	}

	List<String> getSetasKhan() {
		return setasKhan;
	}

	int getPecas() {
		return pecas;
	}

	boolean estaCheia() {
		return pecas >= MAXIMO_PECAS;
	}

	void adicionarPeca() {
		if (estaCheia()) {
			throw new IllegalStateException("Provincia " + id + " ja tem o maximo de pecas");
		}
		pecas++;
	}

	void retirarPeca() {
		if (pecas == 0) {
			throw new IllegalStateException("Provincia " + id + " esta vazia");
		}
		pecas--;
	}
}
