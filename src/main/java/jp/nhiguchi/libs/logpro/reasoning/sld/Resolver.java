package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import static jp.nhiguchi.libs.flist.FList.*;
import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

final class Resolver {
	private Resolver() {
	}

	static FList<SLDBranch> resolve(Program program, FList<SLDBranch> tree) {
		assert !tree.isEmpty();

		var subGoal = tree.head().getGoal().peek().subGoal();

		return switch (subGoal.predicate().type()) {
			case NORMAL  -> resolveNormal(program, tree);
			case SPECIAL -> resolveSpecial(tree);
			case CUT     -> resolveCut(tree);
		};
	}

	private static FList<SLDBranch> resolveNormal(Program program, FList<SLDBranch> tree) {
		var cur = setupInputClauses(program, tree.head());
		var rest = tree.tail();

		var inputClauses = cur.getInputClauses();
		if (inputClauses.isEmpty()) return rest;

		var cutPoint = rest.isEmpty() ? SLDBranch.NULL : rest.head();
		var p = Resolution.resolve(cur.getGoal(), inputClauses.head(), cutPoint);

		return getNextTree(p, cur, rest);
	}

	private static SLDBranch setupInputClauses(Program program, SLDBranch branch) {
		if (branch.isSetUpInputClauses()) return branch;

		var subGoal = branch.getGoal().peek().subGoal();
		var ic = program.clauses(subGoal);
		var inputClauses = (ic instanceof FList<?> f)
				? (FList<Clause>) f
				: flist(ic);

		return SLDBranch.newSLDBranch(branch.getGoal(), branch.getStack(), inputClauses);
	}

	private static FList<SLDBranch> getNextTree(
			Pair<Goal, Resolution> res, SLDBranch cur, FList<SLDBranch> rest) {
		if (res == null) return getRestTree(cur, rest);
		if (cur.getStack().isLoop(res.get1st())) return getRestTree(cur, rest);

		var newBranch = SLDBranch.newSLDBranch(
				res.get1st(), cur.getStack().push(res.get2nd()), null);
		return getRestTree(cur, rest).prepend(newBranch);
	}

	private static FList<SLDBranch> getRestTree(SLDBranch cur, FList<SLDBranch> rest) {
		if (cur.getInputClauses() == null) return rest;

		var restClauses = cur.getInputClauses().tail();
		if (restClauses.isEmpty()) return rest;

		return rest.prepend(SLDBranch.newSLDBranch(cur.getGoal(), cur.getStack(), restClauses));
	}

	private static FList<SLDBranch> resolveSpecial(FList<SLDBranch> tree) {
		var cur = tree.head();
		var rest = tree.tail();
		var cutPoint = rest.isEmpty() ? SLDBranch.NULL : rest.head();
		var p = Resolution.resolveSpecial(cur.getGoal(), cutPoint);
		return getNextTree(p, cur, rest);
	}

	private static FList<SLDBranch> resolveCut(FList<SLDBranch> tree) {
		var cur = tree.head();
		var rest = tree.tail();

		assert cur.getGoal().peek().subGoal().isCut();

		var p = Resolution.resolveCut(cur.getGoal());
		var newBranch = SLDBranch.newSLDBranch(
				p.get1st(), cur.getStack().push(p.get2nd()), null);
		return doCut(cur, rest).prepend(newBranch);
	}

	private static FList<SLDBranch> doCut(SLDBranch cur, FList<SLDBranch> rest) {
		var ent = cur.getGoal().peek();
		assert ent.subGoal().isCut();

		var cutTree = FList.<SLDBranch>flist();
		var cutPoint = ent.cutPoint();
		boolean isCutting = true;

		for (var b : rest) {
			if (b.equals(cutPoint)) isCutting = false;

			var cut = isCutting
					? SLDBranch.newSLDBranch(b.getGoal(), b.getStack(), flist())
					: b;
			cutTree = cons(cut, cutTree);
		}

		return cutTree.reverse();
	}
}
