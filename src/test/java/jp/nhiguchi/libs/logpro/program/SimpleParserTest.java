/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program;

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
public class SimpleParserTest {
	public SimpleParserTest() {
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
	 * Test of variable method, of class SimpleParser2.
	 */
	@Test
	public void testVariable() {
		System.out.println("variable");
		Variable expResult;
		Variable result;

		expResult = Variable.create("X");
		result = SimpleParser.variable("X");
		assertEquals(expResult, result);

		expResult = Variable.create("_X");
		result = SimpleParser.variable("_X");
		assertEquals(expResult, result);

		try {
			result = SimpleParser.variable("atom");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	/**
	 * Test of atom method, of class SimpleParser2.
	 */
	@Test
	public void testAtom() {
		System.out.println("atom");
		Atom expResult;
		Atom result;

		expResult = Atom.create("ann");
		result = SimpleParser.atom("ann");
		assertEquals(expResult, result);

		expResult = Atom.create("av_98_ingram");
		result = SimpleParser.atom("av_98_ingram");
		assertEquals(expResult, result);

		try {
			result = SimpleParser.atom("BadAtom");
			fail();
		} catch (IllegalArgumentException expected) {
		}
	}

	/**
	 * Test of compoundTerm method, of class SimpleParser2.
	 */
	@Test
	public void testCompoundTerm() {
		System.out.println("compoundTerm");
		CompoundTerm expResult;
		CompoundTerm result;

		expResult = CompoundTerm.create(
				Functor.create("foo", 2),
				Atom.create("bar"), Variable.create("Baz"));
		result = SimpleParser.compoundTerm("foo(bar, Baz)");
		assertEquals(expResult, result);
	}

	/**
	 * Test of term method, of class SimpleParser2.
	 */
	@Test
	public void testTerm() {
		System.out.println("term");
		Term expResult;
		Term result;

		expResult = CompoundTerm.create(
				Functor.create("foo", 2),
				CompoundTerm.create(
				Functor.create("bar", 1), Variable.create("X")),
				Variable.create("Baz"));
		result = SimpleParser.term("foo(bar(X), Baz)");
		assertEquals(expResult, result);
	}

	/**
	 * Test of literal method, of class SimpleParser2.
	 */
	@Test
	public void testLiteral() {
		System.out.println("literal");
		AtomicFormula expResult;
		AtomicFormula result;

		expResult = AtomicFormula.create(
				Predicate.create("mortal", 1), Atom.create("man"));
		result = SimpleParser.literal("mortal(man)");
		assertEquals(expResult, result);

		expResult = AtomicFormula.CUT;
		result = SimpleParser.literal("!");

		expResult = AtomicFormula.create(
				Predicate.create("void", 0));
		result = SimpleParser.literal("void");
	}

	/**
	 * Test of fact method, of class SimpleParser2.
	 */
	@Test
	public void testFact() {
		System.out.println("fact");
		Clause expResult;
		Clause result;

		expResult = Clause.fact(
				AtomicFormula.create(
				Predicate.create("mortal", 1), Atom.create("man")));
		result = SimpleParser.fact("mortal(man).");
		assertEquals(expResult, result);

		expResult = Clause.fact(
				AtomicFormula.create(
				Predicate.create("void", 0)));
	}

	/**
	 * Test of query method, of class SimpleParser2.
	 */
	@Test
	public void testQuery() {
		System.out.println("query");
		Clause expResult;
		Clause result;

		expResult = Clause.query(
				AtomicFormula.create(
				Predicate.create("parent", 2), Atom.create("ann"), Atom.create("bob")),
				AtomicFormula.create(
				Predicate.create("female", 1), Atom.create("ann")));
		result = SimpleParser.query("?- parent(ann, bob), female(ann).");
		assertEquals(expResult, result);
	}

	/**
	 * Test of clause method, of class SimpleParser2.
	 */
	@Test
	public void testClause() {
		System.out.println("clause");
		Clause expResult;
		Clause result;

		expResult = Clause.clause(
				AtomicFormula.create(
				Predicate.create("mother", 2), Variable.create("X"), Variable.create("Y")),
				AtomicFormula.create(
				Predicate.create("parent", 2), Variable.create("X"), Variable.create("Y")),
				AtomicFormula.create(
				Predicate.create("female", 1), Variable.create("X")));
		result = SimpleParser.clause("mother(X, Y) :- parent(X, Y), female(X).");
		assertEquals(expResult, result);
	}
}
