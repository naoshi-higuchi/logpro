package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.Iterator;
import java.util.Map;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;
import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author naoshi
 */
public class SampleCodeTest {
	public SampleCodeTest() {
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
	 * Test of sample code.
	 */
	@Test
	public void testSampleCode() {
		System.out.println("Sample code");

		SimpleProgramBuilder builder = new SimpleProgramBuilder();

		builder.add(fact("parent(ann, cate)."));
		builder.add(fact("parent(bob, cate)."));
		builder.add(fact("female(ann)."));
		builder.add(fact("male(bob)."));

		builder.add(clause("mother(X, Y) :- parent(X, Y), female(X)."));
		builder.add(clause("father(X, Y) :- parent(X, Y), male(X)."));

		Program program = builder.toProgram();

		Clause goal = query("?- mother(X, cate).");
		Iterable<Map<Variable, Term>> answers = SLD.solve(program, goal);
		for (Map<Variable, Term> ans : answers) {
			System.out.println(ans);
		}

		{
			answers = SLD.solve(program, goal);
			Iterator<Map<Variable, Term>> it = answers.iterator();
			assertTrue(it.hasNext());
			Map<Variable, Term> answer = it.next();
			assertEquals(1, answer.size());
			assertEquals(Atom.create("ann"), answer.get(Variable.create("X")));
			assertFalse(it.hasNext());
		}
	}
}
