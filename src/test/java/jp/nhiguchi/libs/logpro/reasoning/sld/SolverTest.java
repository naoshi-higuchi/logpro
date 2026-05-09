package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;
import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;

import static jp.nhiguchi.libs.logpro.reasoning.sld.Solver.Solution;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SolverTest {
	private static Program fProgFamily;
	private static Program fProgGeom;

	public SolverTest() {
	}

	@BeforeAll
	public static void setUpClass() throws Exception {
		setUpProgFamily();
		setUpProgGeom();
	}

	private static void setUpProgFamily() {
		var builder = new SimpleProgramBuilder();

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
		var builder = new SimpleProgramBuilder();

		builder.add(fact("vertical(seg(point(X, Y), point(X, Y1)))."));
		builder.add(fact("horizontal(seg(point(X, Y), point(X1, Y)))."));

		fProgGeom = builder.toProgram();
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
		expResult = Map.of(variable("X"), term("cate"));
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult, result);

		program = fProgFamily;
		goal = query("?- sister(X, dan).");
		expResult = Map.of(variable("X"), term("elen"));
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult, result);

		program = fProgFamily;
		goal = query("?- predecessor(X, gene).");
		var expRes = Set.of(
				Map.of(variable("X"), term("ann")),
				Map.of(variable("X"), term("bob")),
				Map.of(variable("X"), term("cate")),
				Map.of(variable("X"), term("elen")),
				Map.of(variable("X"), term("fred")));
		var res = new HashSet<Map<Variable, Term>>();
		Solution sol = Solver.solve(program, Solver.getInitialTree(goal));
		while (sol != null) {
			res.add(sol.cas());
			if (sol.restTree().isEmpty()) break;
			sol = Solver.solve(program, sol.restTree());
		}
		assertEquals(expRes, res);

		program = fProgGeom;
		goal = query("?- vertical(seg(point(x1, y1), point(x1, y2))).");
		expResult = Collections.emptyMap();
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult, result);

		program = fProgGeom;
		goal = query("?- vertical(seg(point(x1, y1), point(x2, Y))).");
		assertNull(Solver.solve(program, Solver.getInitialTree(goal)));

		program = fProgGeom;
		goal = query("?- horizontal(seg(point(x1, y1), point(x2, Y))).");
		expResult = Map.of(variable("Y"), term("y1"));
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult, result);

		program = fProgGeom;
		goal = query("?- vertical(seg(point(x1, y1), P)).");
		expResult = Map.of(variable("P"), term("point(x1, Y)"));
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult.size(), result.size());
		assertTrue(Util.isAlphaEquivalent(
				expResult.get(variable("P")), result.get(variable("P"))));

		program = fProgGeom;
		goal = query("?- vertical(S), horizontal(S).");
		expResult = Map.of(variable("S"), term("seg(point(X, Y), point(X, Y))"));
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult.size(), result.size());
		assertTrue(Util.isAlphaEquivalent(
				expResult.get(variable("S")), result.get(variable("S"))));
	}

	private void testSolve_special() {
		Program program;
		Clause goal;
		Map<Variable, Term> expResult;
		Map<Variable, Term> result;

		program = new SimpleProgramBuilder().toProgram();

		Predicate.Evaluable evalAtom = args -> {
			assert args.size() == 1;
			var t = args.get(0);
			if (!(t instanceof Atom)) return null;
			return new Predicate.Evaluable.Eval() {
				public Clause getResolvent() { return Clause.emptyQuery(); }
				public Map<Variable, ? extends Term> getMGU() { return Collections.emptyMap(); }
			};
		};

		var predAtom = Predicate.createSpecial("atom", 1, evalAtom);

		goal = Clause.query(AtomicFormula.create(predAtom, Atom.create("ann")));
		expResult = Collections.emptyMap();
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult, result);

		goal = Clause.query(AtomicFormula.create(predAtom, Variable.create("X")));
		assertNull(Solver.solve(program, Solver.getInitialTree(goal)));

		Predicate.Evaluable evalMatch = args -> {
			assert args.size() == 2;
			var t1 = args.get(0);
			var t2 = args.get(1);

			var dummyFact1 = AtomicFormula.create(Predicate.create("dummy", 1), t1);
			var dummyFact2 = AtomicFormula.create(Predicate.create("dummy", 1), t2);
			final var mgu = Unification.getMGU(dummyFact1, dummyFact2);
			if (mgu == null) return null;

			return new Predicate.Evaluable.Eval() {
				public Clause getResolvent() { return Clause.emptyQuery(); }
				public Map<Variable, ? extends Term> getMGU() { return mgu; }
			};
		};

		var predMatch = Predicate.createSpecial("match", 2, evalMatch);

		goal = Clause.query(AtomicFormula.create(predMatch, Atom.create("ann"), Variable.create("X")));
		expResult = Map.of(variable("X"), term("ann"));
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult, result);

		goal = Clause.query(AtomicFormula.create(predMatch, Atom.create("ann"), Atom.create("bob")));
		assertNull(Solver.solve(program, Solver.getInitialTree(goal)));

		goal = Clause.query(AtomicFormula.create(
				predMatch, term("couple(adam, Wife)"), term("couple(Husband, eve)")));
		expResult = Map.of(variable("Husband"), term("adam"), variable("Wife"), term("eve"));
		result = Solver.solve(program, Solver.getInitialTree(goal)).cas();
		assertEquals(expResult, result);
	}
}
