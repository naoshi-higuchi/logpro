package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;
import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VariantTest {

	@Test
	public void testCreate_noConflict() {
		// Clause uses Y, goal uses X — no overlap, no renaming needed
		Clause c = clause("p(Y) :- q(Y).");
		Clause goal = query("?- p(X).");
		Variant v = Variant.create(c, goal);
		assertTrue(v.getSubstitute().isEmpty());
		assertEquals(c, v.getInstance());
	}

	@Test
	public void testCreate_conflict_variableIsRenamed() {
		// Clause uses X, goal also uses X — X must be renamed
		Clause c = clause("p(X) :- q(X).");
		Clause goal = query("?- p(X).");
		Variant v = Variant.create(c, goal);
		assertTrue(v.getSubstitute().containsKey(variable("X")));
		Variable renamed = v.getSubstitute().get(variable("X"));
		Set<Variable> goalVars = Util.collectVariables(goal);
		assertFalse(goalVars.contains(renamed));
	}

	@Test
	public void testCreate_conflict_instanceUsesRenamedVariable() {
		// The original X must not appear in the instance; the renamed variable must
		Clause c = clause("p(X) :- q(X).");
		Clause goal = query("?- p(X).");
		Variant v = Variant.create(c, goal);
		Variable renamed = v.getSubstitute().get(variable("X"));
		assertFalse(v.getInstance().head().args().contains(variable("X")));
		assertTrue(v.getInstance().head().args().contains(renamed));
	}

	@Test
	public void testCreate_multipleConflicts() {
		// Clause uses X and Y, goal also uses X and Y — both must be renamed
		Clause c = clause("p(X, Y) :- q(X, Y).");
		Clause goal = query("?- p(X, Y).");
		Variant v = Variant.create(c, goal);
		assertEquals(2, v.getSubstitute().size());
		assertTrue(v.getSubstitute().containsKey(variable("X")));
		assertTrue(v.getSubstitute().containsKey(variable("Y")));
		Set<Variable> goalVars = Util.collectVariables(goal);
		for (Variable renamed : v.getSubstitute().values()) {
			assertFalse(goalVars.contains(renamed));
		}
	}
}
