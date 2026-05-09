/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.tuple.Pair;

import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Naoshi Higuchi
 */
public class ResolutionTest {
	public ResolutionTest() {
	}

	@BeforeAll
	public static void setUpClass() throws Exception {
	}

	@AfterAll
	public static void tearDownClass() throws Exception {
	}

	@BeforeEach
	public void setUp() {
	}

	@AfterEach
	public void tearDown() {
	}

	/**
	 * Test of resolve method, of class Resolution.
	 */
	@Test
	public void testResolve() {
		System.out.println("resolve");
		Goal goal;
		Clause inputClause;
		Goal expResolvent;
		Map<Variable, ? extends Term> expMGU;
		Pair<Goal, Resolution> result;

		goal = Goal.newInitialGoal(query("?- p(X), q(X)."));
		inputClause = clause("p(a) :- r(a).");
		expResolvent = Goal.newGoal(query("?- r(a), q(a)."), null);
		expMGU = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("a"));
			}
		};
		result = Resolution.resolve(goal, inputClause, null);
		assertEquals(expResolvent, result.get1st());
		assertEquals(goal, result.get2nd().getGoal());
		assertEquals(expMGU, result.get2nd().getMGU());
	}
}
