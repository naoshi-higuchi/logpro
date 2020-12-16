package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;
import java.util.concurrent.*;

import jp.nhiguchi.libs.flist.*;
import static jp.nhiguchi.libs.flist.FList.*;
import jp.nhiguchi.libs.tuple.*;
import static jp.nhiguchi.libs.tuple.Pair.*;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

/**
 *
 * @author Naoshi Higuchi
 */
final class Solver {
	static class Solution {
		private final Map<Variable, Term> fCAS;
		private final FList<SLDBranch> fRestTree;

		private Solution(Map<Variable, Term> cas, FList<SLDBranch> restTree) {
			fCAS = cas;
			fRestTree = restTree;
		}

		Map<Variable, Term> getCAS() {
			return fCAS;
		}

		FList<SLDBranch> getRestTree() {
			return fRestTree;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof Solution)) return false;

			Solution rhs = (Solution) obj;

			return Objects.equals(fCAS, rhs.fCAS)
					&& Objects.equals(fRestTree, rhs.fRestTree);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(fCAS)
					+ Objects.hashCode(fRestTree);
		}

		@Override
		public String toString() {
			return String.format("<%s, %s>", fCAS, fRestTree);
		}
	}

	private Solver() {
	}

	static FList<SLDBranch> getInitialTree(Clause initialGoal) {
		Goal initGoal = Goal.newInitialGoal(initialGoal);
		SLDBranch b = SLDBranch.newSLDBranch(
				initGoal, Stack.emptyStack(), null);

		return flist(b);
	}

	/**
	 *
	 * @param program
	 * @param tree
	 * @return Pair<cas, tree>, if solved. null, if failed.
	 */
	static Solution solve(
			Program program, FList<SLDBranch> tree) {
		if (program == null || tree == null) {
			throw new NullPointerException();
		}

		if (tree.isEmpty()) return null;

		while (!isSolved(tree)) {
			tree = Resolver.resolve(program, tree);

			if (tree.isEmpty()) return null;
		}

		assert (!tree.isEmpty());

		Map<Variable, Term> cas = getComputedAnswerSubstitution(tree.head());

		return new Solution(cas, tree.tail());
	}

	static Set<Map<Variable, Term>> solveAll(
			Program program, Clause query, ExecutorService execSrv) {
		ExecutorCompletionService<FList<SLDBranch>> ecs = new ExecutorCompletionService(execSrv);
		FList<SLDBranch> tree = getInitialTree(query);

		Set<Map<Variable, Term>> cases = new HashSet<Map<Variable, Term>>();

		int nTasks = 0;
		while (nTasks > 0 || !tree.isEmpty()) {
			nTasks += submit(ecs, program, tree);
			Pair<Integer, FList<SLDBranch>> p = take(ecs, nTasks);
			nTasks = p.get1st();
			FList<SLDBranch> res = p.get2nd();

			if (res == null || res.isEmpty()) {
				tree = flist();
				continue;
			}

			if (isSolved(res)) {
				cases.add(
						getComputedAnswerSubstitution(
						res.head()));
				tree = res.tail();
			} else tree = res;
		}

		return cases;
	}

	private static boolean isSolved(FList<SLDBranch> tree) {
		assert (!tree.isEmpty());

		return tree.head().isSucceeded();
	}

	private static Map<Variable, Term> getComputedAnswerSubstitution(
			SLDBranch branch) {
		Iterator<Resolution> it = branch.getStack().iterator();

		assert (it.hasNext());
		Resolution bottom = it.next();
		Set<Variable> vars = Util.collectVariables(bottom.getGoal().toClause());

		// Inverse CAS
		Map<Term, Variable> icas = new HashMap<Term, Variable>();
		for (Variable v : vars) {
			icas.put(v, v);
		}

		for (Resolution res : branch.getStack()) {
			Map<Variable, ? extends Term> mgu = res.getMGU();
			for (Map.Entry<Variable, ? extends Term> ent : mgu.entrySet()) {
				Variable v = ent.getKey();
				Term t = icas.get(v);
				if (t != null) {
					Variable org = icas.get(v);

					icas.remove(v);
					icas.put(ent.getValue(), org);
				}
			}
		}

		Map<Variable, Term> cas = new HashMap<Variable, Term>();
		for (Map.Entry<Term, Variable> ent : icas.entrySet()) {
			cas.put(ent.getValue(), ent.getKey());
		}

		return cas;
	}

	private static Callable<FList<SLDBranch>> getCallable(
			final Program program, final FList<SLDBranch> tree) {
		return new Callable<FList<SLDBranch>>() {
			public FList<SLDBranch> call() throws Exception {
				return Resolver.resolve(program, tree);
			}
		};
	}

	private static int submit(
			ExecutorCompletionService<FList<SLDBranch>> ecs,
			final Program program, FList<SLDBranch> tree) {
		int nTasks = 0;

		if (tree.isEmpty()) return nTasks;

		if (tree.head().isForkSuppressiveZone()) {
			ecs.submit(getCallable(program, tree));
			++nTasks;
			return nTasks;
		}

		for (final SLDBranch b : tree) {
			ecs.submit(getCallable(program, flist(b)));
			++nTasks;
		}

		return nTasks;
	}

	private static FList<SLDBranch> take(
			ExecutorCompletionService<FList<SLDBranch>> ecs) {
		try {
			return ecs.take().get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private static Pair<Integer, FList<SLDBranch>> take(
			ExecutorCompletionService<FList<SLDBranch>> ecs, int nTasks) {
		FList<SLDBranch> res;

		do {
			res = take(ecs);
			--nTasks;
		} while (res == null && nTasks > 0);

		return newPair(nTasks, res);
	}
}
