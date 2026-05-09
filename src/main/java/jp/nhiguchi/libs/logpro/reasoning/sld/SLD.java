package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;
import java.util.concurrent.*;

import jp.nhiguchi.libs.flist.*;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

import static jp.nhiguchi.libs.logpro.reasoning.sld.Solver.Solution;

/**
 * Public API for Selective Linear Definite clause (SLD) resolution.
 *
 * <p>SLD resolution is the standard inference mechanism for Prolog-style logic programs.
 * Given a {@link Program} (a set of facts and rules) and a query {@link Clause},
 * the solver finds all variable bindings (computed answer substitutions, CAS) that
 * satisfy the query.
 *
 * <p>Example usage:
 * <pre>{@code
 * Program program = new SimpleProgramBuilder()
 *     .add(SimpleParser.fact("parent(ann, bob)."))
 *     .add(SimpleParser.fact("female(ann)."))
 *     .add(SimpleParser.clause("mother(X, Y) :- parent(X, Y), female(X)."))
 *     .toProgram();
 *
 * Clause query = SimpleParser.query("?- mother(X, bob).");
 * Map<Variable, Term> answer = SLD.solveOne(program, query);
 * }</pre>
 */
public final class SLD {
	private static class CASes implements Iterable<Map<Variable, Term>> {
		private final Iterator<Map<Variable, Term>> fIterator;

		private CASes(Program program, FList<SLDBranch> tree) {
			fIterator = new CASIterator(program, tree);
		}

		@Override
		public Iterator<Map<Variable, Term>> iterator() {
			return fIterator;
		}
	}

	private static class CASIterator implements Iterator<Map<Variable, Term>> {
		private final Program fProgram;
		private FList<SLDBranch> fTree;
		private Map<Variable, Term> fNext;

		private CASIterator(Program program, FList<SLDBranch> tree) {
			fProgram = program;
			fTree = tree;
		}

		@Override
		public boolean hasNext() {
			if (fNext != null) return true;

			var sol = Solver.solve(fProgram, fTree);
			if (sol == null) return false;

			fNext = sol.cas();
			fTree = sol.restTree();
			return true;
		}

		@Override
		public Map<Variable, Term> next() {
			if (!hasNext()) throw new NoSuchElementException();
			var res = fNext;
			fNext = null;
			return res;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported.");
		}
	}

	/**
	 * Returns a lazy iterable over all computed answer substitutions (CAS) for the query.
	 *
	 * <p>Solutions are computed on demand as the returned iterable is traversed.
	 * Each element is a {@code Map<Variable, Term>} binding the query's variables
	 * to their answer values.
	 *
	 * @param program the logic program (facts and rules); must not be {@code null}
	 * @param query   the query clause; must not be {@code null}
	 * @return a lazy iterable of answer substitutions; empty if no answer exists
	 */
	public static Iterable<Map<Variable, Term>> solve(Program program, Clause query) {
		return new CASes(program, Solver.getInitialTree(query));
	}

	/**
	 * Returns the first computed answer substitution for the query, or {@code null} if none exists.
	 *
	 * @param program the logic program; must not be {@code null}
	 * @param query   the query clause; must not be {@code null}
	 * @return the first answer substitution, or {@code null} if no answer is found
	 */
	public static Map<Variable, Term> solveOne(Program program, Clause query) {
		var it = solve(program, query).iterator();
		return it.hasNext() ? it.next() : null;
	}

	/**
	 * Returns the set of all computed answer substitutions for the query.
	 *
	 * <p>This method exhausts all solutions before returning. Use {@link #solve} for
	 * lazy, on-demand evaluation.
	 *
	 * @param program the logic program; must not be {@code null}
	 * @param query   the query clause; must not be {@code null}
	 * @return a set of all answer substitutions; empty if no answer is found
	 */
	public static Set<Map<Variable, Term>> solveAll(Program program, Clause query) {
		var cases = new HashSet<Map<Variable, Term>>();
		for (var cas : solve(program, query)) cases.add(cas);
		return cases;
	}

	/**
	 * Returns the set of all computed answer substitutions using parallel solving.
	 *
	 * <p>The provided {@link ExecutorService} is used to evaluate SLD branches
	 * concurrently. The caller is responsible for managing the executor's lifecycle.
	 *
	 * <p><strong>Note:</strong> due to the cut operator's interaction with parallel
	 * evaluation, results may differ from sequential solving for programs that use cut.
	 *
	 * @param program  the logic program; must not be {@code null}
	 * @param query    the query clause; must not be {@code null}
	 * @param execSrv  the executor service for parallel branch evaluation
	 * @return a set of all answer substitutions; empty if no answer is found
	 */
	public static Set<Map<Variable, Term>> solveAll(
			Program program, Clause query, ExecutorService execSrv) {
		return Solver.solveAll(program, query, execSrv);
	}
}
