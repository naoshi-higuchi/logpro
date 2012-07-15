package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import static jp.nhiguchi.libs.flist.FList.*;
import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

/**
 *
 * @author Naoshi Higuchi
 */
final class Resolver {
	private Resolver() {
	}

	static FList<SLDBranch> resolve(
			Program program, FList<SLDBranch> tree) {
		assert (!tree.isEmpty());

		AtomicFormula subGoal = tree.head().getGoal().peek().getSubGoal();

		if (subGoal.isNormal()) return resolveNormal(program, tree);

		if (subGoal.isSpecial()) return resolveSpecial(tree);

		if (subGoal.isCut()) return resolveCut(tree);

		throw new RuntimeException();
	}

	private static FList<SLDBranch> resolveNormal(
			Program program, FList<SLDBranch> tree) {
		SLDBranch cur = tree.head();
		FList<SLDBranch> rest = tree.tail();

		cur = setupInputClauses(program, cur);

		FList<Clause> inputClauses = cur.getInputClauses();
		if (inputClauses.isEmpty()) return rest;

		SLDBranch cutPoint = rest.isEmpty() ? SLDBranch.NULL : rest.head();

		Pair<Goal, Resolution> p = Resolution.resolve(
				cur.getGoal(), inputClauses.head(), cutPoint);

		return getNextTree(p, cur, rest);
	}

	private static SLDBranch setupInputClauses(
			Program program, SLDBranch branch) {
		if (branch.isSetUpInputClauses()) return branch;

		AtomicFormula subGoal = branch.getGoal().peek().getSubGoal();

		List<Clause> ic = program.clauses(subGoal);
		FList<Clause> inputClauses;
		if (ic instanceof FList) {
			inputClauses = (FList<Clause>) ic;
		} else {
			inputClauses = flist(ic);
		}


		return SLDBranch.newSLDBranch(
				branch.getGoal(),
				branch.getStack(),
				inputClauses);
	}

	private static FList<SLDBranch> getNextTree(
			Pair<Goal, Resolution> res, SLDBranch cur,
			FList<SLDBranch> rest) {
		if (res == null) return getRestTree(cur, rest);

		if (cur.getStack().isLoop(res.get1st())) {
			return getRestTree(cur, rest);
		}

		SLDBranch newBranch = SLDBranch.newSLDBranch(
				res.get1st(), cur.getStack().push(res.get2nd()), null);

		return getRestTree(cur, rest).prepend(newBranch);
	}

	private static FList<SLDBranch> getRestTree(
			SLDBranch cur, FList<SLDBranch> rest) {
		if (cur.getInputClauses() == null) return rest;

		FList<Clause> restClauses = cur.getInputClauses().tail();

		if (restClauses.isEmpty()) return rest;

		return rest.prepend(
				SLDBranch.newSLDBranch(
				cur.getGoal(), cur.getStack(), restClauses));
	}

	private static FList<SLDBranch> resolveSpecial(FList<SLDBranch> tree) {
		SLDBranch cur = tree.head();
		FList<SLDBranch> rest = tree.tail();
		SLDBranch cutPoint = rest.isEmpty() ? SLDBranch.NULL : rest.head();

		Pair<Goal, Resolution> p = Resolution.resolveSpecial(cur.getGoal(), cutPoint);

		return getNextTree(p, cur, rest);
	}

	private static FList<SLDBranch> resolveCut(FList<SLDBranch> tree) {
		SLDBranch cur = tree.head();
		FList<SLDBranch> rest = tree.tail();

		assert (cur.getGoal().peek().getSubGoal().isCut());

		Pair<Goal, Resolution> p = Resolution.resolveCut(cur.getGoal());

		SLDBranch newBranch = SLDBranch.newSLDBranch(
				p.get1st(), cur.getStack().push(p.get2nd()), null);

		return doCut(cur, rest).prepend(newBranch);
	}

	private static FList<SLDBranch> doCut(SLDBranch cur, FList<SLDBranch> rest) {
		Goal.Entry ent = cur.getGoal().peek();
		assert (ent.getSubGoal().isCut());

		FList<SLDBranch> cutTree = flist();
		SLDBranch cutPoint = ent.getCutPoint();
		boolean isCutting = true;
		for (SLDBranch b : rest) {
			if (b.equals(cutPoint)) isCutting = false;

			SLDBranch cut;
			FList<Clause> empty = flist();

			if (isCutting) {
				cut = SLDBranch.newSLDBranch(
						b.getGoal(), b.getStack(), empty);
			} else {
				cut = b; // b doesn't get cut.
			}

			cutTree = cons(cut, cutTree);
		}

		return cutTree.reverse();
	}
}
