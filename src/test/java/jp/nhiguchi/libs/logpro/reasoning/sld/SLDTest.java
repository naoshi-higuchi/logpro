/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;
import java.util.concurrent.*;

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
 * @author Naoshi Higuchi
 */
public class SLDTest {
	private static Program fProgFamily;
	private static Program fProgWithCut;
	private static Program fProgWithLoop;
	private static Program fProgWith0ArgLiteral;
	private static ExecutorService fExecSrv;
	private static final int NLOOP = 100;

	static {
		int nCPUs = Runtime.getRuntime().availableProcessors();
		System.out.println("availableProcessors: " + nCPUs);
		//fExecSrv = Executors.newFixedThreadPool(nCPUs);
		fExecSrv = new ThreadPoolExecutor(nCPUs, nCPUs, 7L, TimeUnit.SECONDS, new BlockingLIFOQueue());
	}

	public SLDTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		setUpProgFamily();
		setUpProgWithCut();
		setUpProgWithLoop();
		setUpProgWith0ArgLiteral();
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

	private static void setUpProgWithCut() {
		SimpleProgramBuilder builder = new SimpleProgramBuilder();

		/*
		 * if-then-else-cut.
		 * ?- p(X) returns X=a.
		 */
		builder.add(clause("p(X) :- q(X), !, r(X)."));
		builder.add(clause("p(X) :- s(X)."));
		builder.add(fact("q(a)."));
		builder.add(fact("r(a)."));
		builder.add(fact("s(b)."));

		/*
		 * if-then-else-cut.
		 * ?- p2(X) returns [{X=a}, {X=b}].
		 */
		builder.add(clause("p2(X) :- s(X)."));
		builder.add(clause("p2(X) :- q(X), !, r(X)."));

		/*
		 * selective-cut.
		 * ?- p3(X) returns {{X=a}} or {{X=b}}, not {{X=a}, {X=b}}.
		 */
		builder.add(clause("p3(X) :- u(X), !, v(Y)."));
		builder.add(fact("u(a)."));
		builder.add(fact("u(b)."));
		builder.add(fact("v(c)."));

		/*
		 * two-cuts
		 * ?- p4(X) returns {{Y=a}} or {{Y=b}}.
		 */
		builder.add(clause("p4(Y) :- x(X), !, y(Y), !, z(Z)."));
		builder.add(fact("x(a)."));
		builder.add(fact("x(b)."));
		builder.add(fact("y(a)."));
		builder.add(fact("y(b)."));
		builder.add(fact("z(a)."));
		builder.add(fact("z(b)."));

		/*
		 * green cut.
		 */
		builder.add(clause("f(X, one) :- isA(X), !."));
		builder.add(clause("f(X, two) :- isB(X), !."));
		builder.add(clause("f(X, three) :- isC(X), !."));
		builder.add(fact("isA(a)."));
		builder.add(fact("isB(b)."));
		builder.add(fact("isC(c)."));

		/*
		 * red cut.
		 */
		builder.add(clause("f2(X, one) :- isA(X), !."));
		builder.add(clause("f2(X, two) :- isB(X), !."));
		builder.add(fact("f2(X, three)."));

		builder.add(fact("both(a)."));
		builder.add(fact("both(b)."));
		builder.add(clause("only(c) :- !."));
		builder.add(fact("only(d)."));
		builder.add(fact("ok(X)."));
		builder.add(clause("f3(X, Y) :- both(X), f4(Y)."));
		builder.add(clause("f4(Y) :- only(Y), !, ok(Y)."));


		fProgWithCut = builder.toProgram();
	}

	private static void setUpProgWithLoop() {
		SimpleProgramBuilder builder = new SimpleProgramBuilder();

		builder.add(clause("path(X, Y) :- arc(X, Z), path(Z, Y)."));
		builder.add(clause("path(X, Y) :- arc(X, Y)."));
		builder.add(fact("arc(a, b)."));
		builder.add(fact("arc(b, a)."));
		builder.add(fact("arc(b, c)."));

		fProgWithLoop = builder.toProgram();
	}

	private static void setUpProgWith0ArgLiteral() {
		SimpleProgramBuilder builder = new SimpleProgramBuilder();
		builder.add(fact("literalWith0Arg."));

		fProgWith0ArgLiteral = builder.toProgram();
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
	 * Test of solve method, of class SLD.
	 */
	@Test
	public void testSolve() {
		System.out.println("solve");
		Program program;
		Clause query;
		Set<Map<Variable, Term>> expResult;
		Set<Map<Variable, Term>> result;

		program = fProgFamily;
		query = query("?- predecessor(X, gene).");
		expResult = new HashSet<Map<Variable, Term>>() {
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

		Iterable<Map<Variable, Term>> it = SLD.solve(program, query);
		result = new HashSet<Map<Variable, Term>>();
		for (Map<Variable, Term> cas : it) {
			result.add(cas);
		}
		assertEquals(expResult, result);
	}

	/**
	 * Test of solveOne method, of class SLD.
	 */
	@Test
	public void testSolveOne() {
		System.out.println("solveOne");
		Program program;
		Clause query;
		Map<Variable, Term> expResult;
		Map<Variable, Term> result;

		program = fProgFamily;
		query = query("?- sister(X, dan).");
		expResult = new HashMap<Variable, Term>() {
			{
				put(variable("X"), term("elen"));
			}
		};
		result = SLD.solveOne(program, query);
		assertEquals(expResult, result);
	}

	private void solveAllAndAssert(
			Program prog, Clause query, Set<Map<Variable, Term>> expResult) {
		Set<Map<Variable, Term>> result;

		result = SLD.solveAll(prog, query);
		assertEquals(expResult, result);

		for (int i = 0; i < NLOOP; ++i) {
			result = SLD.solveAll(prog, query, fExecSrv);
			assertEquals(expResult, result);
		}
	}

	/**
	 * Test of solveAll method, of class SLD.
	 */
	@Test
	public void testSolveAll_Program_Clause() {
		System.out.println("solveAll");
		Program program;
		Clause query;
		Set<Map<Variable, Term>> expResult;
		Set<Map<Variable, Term>> result;

		program = fProgFamily;
		query = query("?- predecessor(X, gene).");
		expResult = new HashSet<Map<Variable, Term>>() {
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
		solveAllAndAssert(program, query, expResult);
	}

	/**
	 * Test of solveAll method, of class SLD.
	 */
	@Test
	public void testSolveAll_3args() {
		System.out.println("solveAll");
		Program program;
		Clause query;
		Set<Map<Variable, Term>> expResult;
		Set result;

		program = fProgFamily;
		query = query("?- predecessor(X, gene).");
		expResult = new HashSet<Map<Variable, Term>>() {
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

		long startTime;
		long estimatedTime;

		startTime = System.nanoTime();
		for (int i = 0; i < 100; ++i) {
			result = SLD.solveAll(program, query);
			assertEquals(expResult, result);
		}
		estimatedTime = System.nanoTime() - startTime;
		System.out.printf("estimatedTime of solveAll with single thread: %d\n", estimatedTime);

		startTime = System.nanoTime();
		for (int i = 0; i < 100; ++i) {
			result = SLD.solveAll(program, query, fExecSrv);
			assertEquals(expResult, result);
		}
		estimatedTime = System.nanoTime() - startTime;
		System.out.printf("estimatedTime of solveAll with multi threads: %d\n", estimatedTime);
	}

	/**
	 * Test of solveAll method, of class SLD.
	 */
	@Test
	public void testSolveAll_Program_Clause_withCut() {
		System.out.println("solveAll");
		Program program;
		Clause query;
		Set<Map<Variable, Term>> expResult;

		program = fProgWithCut;
		query = query("?- p(X).");
		expResult = new HashSet<Map<Variable, Term>>() {
			{
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("a"));
					}
				});
			}
		};
		solveAllAndAssert(program, query, expResult);

		program = fProgWithCut;
		query = query("?- p2(X).");
		expResult = new HashSet<Map<Variable, Term>>() {
			{
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("a"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("b"));
					}
				});
			}
		};
		solveAllAndAssert(program, query, expResult);

		program = fProgWithCut;
		query = query("?- p3(X).");
		expResult = new HashSet<Map<Variable, Term>>() {
			{
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("a"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("b"));
					}
				});
			}
		};
		for (int i = 0; i < NLOOP; ++i) {
			assertFalse(expResult.equals(SLD.solveAll(program, query, fExecSrv)));
		}

		program = fProgWithCut;
		query = query("?- p4(Y).");
		for (int i = 0; i < NLOOP; ++i) {
			assertEquals(1, SLD.solveAll(program, query, fExecSrv).size());
		}

		program = fProgWithCut;
		query = query("?- f(b, Y).");
		expResult = new HashSet<Map<Variable, Term>>() {
			{
				add(new HashMap<Variable, Term>() {
					{
						put(variable("Y"), term("two"));
					}
				});
			}
		};
		solveAllAndAssert(program, query, expResult);

		program = fProgWithCut;
		query = query("?- f2(c, Y).");
		expResult = new HashSet<Map<Variable, Term>>() {
			{
				add(new HashMap<Variable, Term>() {
					{
						put(variable("Y"), term("three"));
					}
				});
			}
		};
		solveAllAndAssert(program, query, expResult);

		program = fProgWithCut;
		query = query("?- f3(X, Y).");
		expResult = new HashSet<Map<Variable, Term>>() {
			{
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("a"));
						put(variable("Y"), term("c"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("b"));
						put(variable("Y"), term("c"));
					}
				});
			}
		};
		solveAllAndAssert(program, query, expResult);
	}

	/**
	 * Test of solveAll method, of class SLD.
	 */
	@Test
	public void testSolveAll_Program_Clause_withLoop() {
		System.out.println("solveAll");
		Program program;
		Clause query;
		Set<Map<Variable, Term>> expResult;

		program = fProgWithLoop;
		query = query("?- path(a, X).");
		expResult = new HashSet<Map<Variable, Term>>() {
			{
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("a"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("b"));
					}
				});
				add(new HashMap<Variable, Term>() {
					{
						put(variable("X"), term("c"));
					}
				});
			}
		};
		solveAllAndAssert(program, query, expResult);
	}

	/**
	 * Test of solveAll method, of class SLD.
	 */
	//@Test
	public void testSolveAll_Program_Clause_with0ArgLiteral() {
		System.out.println("solveAll");
		Program program;
		Clause query;
		Set<Map<Variable, Term>> expResult;

		program = fProgWith0ArgLiteral;
		query = query("?- literalWith0Arg.");
		expResult = new HashSet<Map<Variable, Term>>();
		solveAllAndAssert(program, query, expResult);
	}
}
