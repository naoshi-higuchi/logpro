/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Naoshi Higuchi
 */
public class UnificationTest {
	public UnificationTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of getMGU method, of class Unification.
	 */
	@Test
	public void testGetMGU() {
		System.out.println("calcMGU");
		subtestGetMGU_success();
		subtestGetMGU_fail();
	}

	private void subtestGetMGU_success() {
		AtomicFormula subGoal;
		AtomicFormula inputHead;
		Map expResult;
		Map result;

		subGoal = literal("p(X)");
		inputHead = literal("p(a)");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("a"));
			}
		};
		result = Unification.getMGU(subGoal, inputHead);
		assertEquals(expResult, result);

		subGoal = literal("p(f(X))");
		inputHead = literal("p(f(a))");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("a"));
			}
		};
		result = Unification.getMGU(subGoal, inputHead);
		assertEquals(expResult, result);

		subGoal = literal("p(X, X)");
		inputHead = literal("p(Y, a)");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("a"));
				put(variable("Y"), term("a"));
			}
		};
		result = Unification.getMGU(subGoal, inputHead);
		assertEquals(expResult, result);

		subGoal = literal("p(a, X)");
		inputHead = literal("p(Y, f(Z))");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("f(Z)"));
				put(variable("Y"), term("a"));
			}
		};
		result = Unification.getMGU(subGoal, inputHead);
		assertEquals(expResult, result);

		subGoal = literal("p(Y, f(Z))");
		inputHead = literal("p(a, X)");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("f(Z)"));
				put(variable("Y"), term("a"));
			}
		};
		result = Unification.getMGU(subGoal, inputHead);
		assertEquals(expResult, result);
	}

	private void subtestGetMGU_fail() {
		AtomicFormula subGoal;
		AtomicFormula inputHead;
		Map result;

		subGoal = literal("p(X)");
		inputHead = literal("q(a)");
		result = Unification.getMGU(subGoal, inputHead);
		assertNull(result);

		subGoal = literal("p(a)");
		inputHead = literal("p(b)");
		result = Unification.getMGU(subGoal, inputHead);
		assertNull(result);

		subGoal = literal("p(X, X)");
		inputHead = literal("p(a, b)");
		result = Unification.getMGU(subGoal, inputHead);
		assertNull(result);

		subGoal = literal("p(X, X)");
		inputHead = literal("p(f(Y),Y)");
		result = Unification.getMGU(subGoal, inputHead);
		assertNull(result);
	}
}
