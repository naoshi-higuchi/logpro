package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.Iterator;
import java.util.Map;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;
import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author naoshi
 */
public class SampleCodeTest {
	public SampleCodeTest() {
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
