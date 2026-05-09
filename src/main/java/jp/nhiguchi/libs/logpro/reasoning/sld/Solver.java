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

final class Solver {
	record Solution(Map<Variable, Term> cas, FList<SLDBranch> restTree) {}

	private Solver() {
	}

	static FList<SLDBranch> getInitialTree(Clause initialGoal) {
		var initGoal = Goal.newInitialGoal(initialGoal);
		var b = SLDBranch.newSLDBranch(initGoal, Stack.emptyStack(), null);
		return flist(b);
	}

	static Solution solve(Program program, FList<SLDBranch> tree) {
		if (program == null || tree == null) throw new NullPointerException();
		if (tree.isEmpty()) return null;

		while (!isSolved(tree)) {
			tree = Resolver.resolve(program, tree);
			if (tree.isEmpty()) return null;
		}

		assert !tree.isEmpty();

		var cas = getComputedAnswerSubstitution(tree.head());
		return new Solution(cas, tree.tail());
	}

	static Set<Map<Variable, Term>> solveAll(
			Program program, Clause query, ExecutorService execSrv) {
		var ecs = new ExecutorCompletionService<FList<SLDBranch>>(execSrv);
		var tree = getInitialTree(query);
		var cases = new HashSet<Map<Variable, Term>>();

		int nTasks = 0;
		while (nTasks > 0 || !tree.isEmpty()) {
			nTasks += submit(ecs, program, tree);
			var p = take(ecs, nTasks);
			nTasks = p.get1st();
			var res = p.get2nd();

			if (res == null || res.isEmpty()) {
				tree = flist();
				continue;
			}

			if (isSolved(res)) {
				cases.add(getComputedAnswerSubstitution(res.head()));
				tree = res.tail();
			} else {
				tree = res;
			}
		}

		return cases;
	}

	private static boolean isSolved(FList<SLDBranch> tree) {
		assert !tree.isEmpty();
		return tree.head().isSucceeded();
	}

	private static Map<Variable, Term> getComputedAnswerSubstitution(SLDBranch branch) {
		var it = branch.getStack().iterator();

		assert it.hasNext();
		var bottom = it.next();
		var vars = Util.collectVariables(bottom.getGoal().toClause());

		var icas = new HashMap<Term, Variable>();
		for (var v : vars) icas.put(v, v);

		for (var res : branch.getStack()) {
			for (var ent : res.getMGU().entrySet()) {
				var v = ent.getKey();
				var org = icas.get(v);
				if (org != null) {
					icas.remove(v);
					icas.put(ent.getValue(), org);
				}
			}
		}

		var cas = new HashMap<Variable, Term>();
		for (var ent : icas.entrySet()) cas.put(ent.getValue(), ent.getKey());

		return cas;
	}

	private static int submit(
			ExecutorCompletionService<FList<SLDBranch>> ecs,
			Program program, FList<SLDBranch> tree) {
		if (tree.isEmpty()) return 0;

		if (tree.head().isForkSuppressiveZone()) {
			ecs.submit(() -> Resolver.resolve(program, tree));
			return 1;
		}

		int nTasks = 0;
		for (var b : tree) {
			ecs.submit(() -> Resolver.resolve(program, flist(b)));
			++nTasks;
		}
		return nTasks;
	}

	private static FList<SLDBranch> take(ExecutorCompletionService<FList<SLDBranch>> ecs) {
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
