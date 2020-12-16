/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author Naoshi Higuchi
 */
public final class SLD {
	/**
	 * Computed Answer Substitutions
	 */
	private static class CASes implements Iterable<Map<Variable, Term>> {
		private final Iterator<Map<Variable, Term>> fIterator;

		private CASes(Program program, FList<SLDBranch> tree) {
			fIterator = new CASIterator(program, tree);
		}

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
			fNext = null;
		}

		public boolean hasNext() {
			if (fNext != null) return true;

			Solution sol;
			sol = Solver.solve(fProgram, fTree);

			if (sol == null) return false;

			fNext = sol.getCAS();
			fTree = sol.getRestTree();

			return true;
		}

		public Map<Variable, Term> next() {
			if (!hasNext()) throw new NoSuchElementException();

			Map<Variable, Term> res = fNext;
			fNext = null;

			return res;
		}

		public void remove() {
			throw new UnsupportedOperationException("Not supported.");
		}
	}

	public static Iterable<Map<Variable, Term>> solve(Program program, Clause query) {
		FList<SLDBranch> initTree = Solver.getInitialTree(query);
		return new CASes(program, initTree);
	}

	/**
	 *
	 * @param program
	 * @param query
	 * @return A CAS(Computed Answer Substitution). null, if no answer is found.
	 */
	public static Map<Variable, Term> solveOne(Program program, Clause query) {
		Iterator<Map<Variable, Term>> it = solve(program, query).iterator();
		if (!it.hasNext()) return null;

		return it.next();
	}

	/**
	 *
	 * @param program
	 * @param query
	 * @return A set of CASes(Computed Answer Substitutions). An empty set, if
	 * no answer is found.
	 */
	public static Set<Map<Variable, Term>> solveAll(Program program, Clause query) {
		Set<Map<Variable, Term>> cases = new HashSet<Map<Variable, Term>>();

		for (Map<Variable, Term> cas : solve(program, query)) {
			cases.add(cas);
		}

		return cases;
	}

	public static Set<Map<Variable, Term>> solveAll(
			Program program, Clause query, ExecutorService execSrv) {
		return Solver.solveAll(program, query, execSrv);
	}
}
