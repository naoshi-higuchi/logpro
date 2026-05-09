package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;
import java.util.concurrent.*;

import jp.nhiguchi.libs.flist.*;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

import static jp.nhiguchi.libs.logpro.reasoning.sld.Solver.Solution;

/**
 * Selective Linear Definite clause resolution.
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

	public static Iterable<Map<Variable, Term>> solve(Program program, Clause query) {
		return new CASes(program, Solver.getInitialTree(query));
	}

	public static Map<Variable, Term> solveOne(Program program, Clause query) {
		var it = solve(program, query).iterator();
		return it.hasNext() ? it.next() : null;
	}

	public static Set<Map<Variable, Term>> solveAll(Program program, Clause query) {
		var cases = new HashSet<Map<Variable, Term>>();
		for (var cas : solve(program, query)) cases.add(cas);
		return cases;
	}

	public static Set<Map<Variable, Term>> solveAll(
			Program program, Clause query, ExecutorService execSrv) {
		return Solver.solveAll(program, query, execSrv);
	}
}
