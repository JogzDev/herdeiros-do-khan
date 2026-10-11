package model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite com todos os casos de teste do componente Model.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	PreparacaoTest.class,
	MovimentoTest.class,
	TributoKhanTest.class,
	CidadeTest.class,
	MelhoriaTest.class,
	YurtTest.class,
	ConselheiroTest.class,
	FimDeJogoTest.class,
	RegressaoTest.class
})
public class TodosOsTestes {
}
