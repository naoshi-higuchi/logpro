/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;
import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;

import static jp.nhiguchi.libs.logpro.reasoning.sld.Solver.Solution;

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
public class SolverTest {
	private static Program fProgFamily;
	private static Program fProgGeom;

	public SolverTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpProgFamily();
		setUpProgGeom();
	}

	private static void setUpProgFamily() {
		SimpleProgramBuilder builder = new SimpleProgramBuilder();

		builder.add(fact("parent(ann, cate)."));
		builder.add(fact("parent(bob, cate)."));
		builder.add(fact("parent(cate, dan)."));
		builder.add(fact("parent(cate, elen)."));
		builder.add(fact("parent(elen, gene)."));
		builder.add(fact("parent(fred, gene)."));

		builder.add(fact("female(ann)."));
		builder.add(fact("male(bob)."));
		builder.add(fact("female(cate)."));
		builder.add(fact("male(dan)."));
		builder.add(fact("female(elen)."));
		builder.add(fact("male(fred)."));
		builder.add(fact("male(gene)."));

		builder.add(clause("mother(X, Y) :- parent(X, Y), female(X)."));
		builder.add(clause("father(X, Y) :- parent(X, Y), male(X)."));
		builder.add(clause("sibling(X, Y) :- parent(Z, X), parent(Z, Y)."));
		builder.add(clause("sister(X, Y) :- sibling(X, Y), female(X)."));
		builder.add(clause("brother(X, Y) :- sibling(X, Y), male(X)."));
		builder.add(clause("predecessor(X, Y) :- parent(X, Y)."));
		builder.add(clause("predecessor(X, Y) :- parent(X, Z), predecessor(Z, Y)."));

		fProgFamily = builder.toProgram();
	}

	private static void setUpProgGeom() {
		SimpleProgramBuilder builder = new SimpleProgramBuilder();

		builder.add(fact("vertical(seg(point(X, Y), point(X, Y1)))."));
		builder.add(fact("horizontal(seg(point(X, Y), point(X1, Y)))."));

		fProgGeom = builder.toProgram();
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
	 * Test of solve method, of class Solver.
	 */
	@Test
	public void testSolve() {
		System.out.println("solve");
		testSolve_normal();
		testSolve_special();
	}

	private void testSolve_normal() {
		Program program;
		Clause goal;
		Map<Variable, Term> expResult;
		Map<Variable, Term> result;

		program = fProgFamily;
		goal = query("?- parent(ann, X).");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("cate"));
			}
		};
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult, result);

		program = fProgFamily;
		goal = query("?- sister(X, dan).");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("elen"));
			}
		};
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult, result);

		program = fProgFamily;
		goal = query("?- predecessor(X, gene).");
		Set<Map<Variable, Term>> expRes = new HashSet<Map<Variable, Term>>() {
			{
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("ann"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("bob"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("cate"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("elen"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("fred"));
					}
				});
			}
		};
		Set<Map<Variable, Term>> res = new HashSet<Map<Variable, Term>>();
		Solution sol = Solver.solve(program, Solver.getInitialTree(goal));
		while (sol != null) {
			res.add(sol.getCAS());
			if (sol.getRestTree().isEmpty()) break;
			sol = Solver.solve(program, sol.getRestTree());
		}
		assertEquals(expRes, res);

		program = fProgGeom;
		//goal = query(literal("vertical", term("seg", "point(x1, y1)", "point(x1, y2)")));
		goal = query("?- vertical(seg(point(x1, y1), point(x1, y2))).");
		expResult = Collections.emptyMap();
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult, result);

		program = fProgGeom;
		goal = query("?- vertical(seg(point(x1, y1), point(x2, Y))).");
		assertNull(Solver.solve(program, Solver.getInitialTree(goal)));

		program = fProgGeom;
		goal = query("?- horizontal(seg(point(x1, y1), point(x2, Y))).");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("Y"), term("y1"));
			}
		};
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult, result);

		program = fProgGeom;
		goal = query("?- vertical(seg(point(x1, y1), P)).");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("P"), term("point(x1, Y)"));
			}
		};
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult.size(), result.size());
		assertTrue(Util.isAlphaEquivalent(
				expResult.get(variable("P")), result.get(variable("P"))));

		program = fProgGeom;
		goal = query("?- vertical(S), horizontal(S).");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("S"), term("seg(point(X, Y), point(X, Y))"));
			}
		};
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult.size(), result.size());
		assertTrue(Util.isAlphaEquivalent(
				expResult.get(variable("S")), result.get(variable("S"))));
	}

	private void testSolve_special() {
		Program program;
		Clause goal;
		Map<Variable, Term> expResult;
		Map<Variable, Term> result;

		program = new SimpleProgramBuilder().toProgram(); // empty.

		Predicate.Evaluable evalAtom = new Predicate.Evaluable() {
			public Eval eval(List<? extends Term> args) {
				assert (args.size() == 1);

				Term t = args.get(0);

				if (t instanceof Atom) {
					return new Eval() {
						public Clause getResolvent() {
							return Clause.emptyQuery();
						}

						public Map<Variable, ? extends Term> getMGU() {
							return Collections.EMPTY_MAP;
						}
					};
				}

				return null;
			}
		};

		Predicate predAtom = Predicate.createSpecial("atom", 1, evalAtom);

		goal = Clause.query(AtomicFormula.create(
				predAtom, Atom.create("ann")));
		expResult = Collections.EMPTY_MAP;
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult, result);

		goal = Clause.query(AtomicFormula.create(
				predAtom, Variable.create("X")));
		Solution res = Solver.solve(program, Solver.getInitialTree(goal));
		assertNull(res);

		Predicate.Evaluable evalMatch = new Predicate.Evaluable() {
			public Eval eval(List<? extends Term> args) {
				assert (args.size() == 2);

				Term t1 = args.get(0);
				Term t2 = args.get(1);

				AtomicFormula dummyFact1 = AtomicFormula.create(
						Predicate.create(
						"dummy", 1), t1);
				AtomicFormula dummyFact2 = AtomicFormula.create(
						Predicate.create(
						"dummy", 1), t2);

				final Map<Variable, ? extends Term> mgu = Unification.getMGU(
						dummyFact1, dummyFact2);

				if (mgu == null) return null;

				return new Eval() {
					public Clause getResolvent() {
						return Clause.emptyQuery();
					}

					public Map<Variable, ? extends Term> getMGU() {
						return mgu;
					}
				};
			}
		};

		Predicate predMatch = Predicate.createSpecial("match", 2, evalMatch);

		goal = Clause.query(AtomicFormula.create(
				predMatch, Atom.create("ann"), Variable.create("X")));
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("ann"));
			}
		};
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult, result);

		goal = Clause.query(AtomicFormula.create(
				predMatch, Atom.create("ann"), Atom.create("bob")));
		assertNull(
				Solver.solve(program, Solver.getInitialTree(goal)));

		goal = Clause.query(AtomicFormula.create(
				predMatch, term("couple(adam, Wife)"), term("couple(Husband, eve)")));
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("Husband"), term("adam"));
				put(variable("Wife"), term("eve"));
			}
		};
		result = Solver.solve(program, Solver.getInitialTree(goal)).getCAS();
		assertEquals(expResult, result);
	}
}
