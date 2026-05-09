package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

final class Resolution {
	private final Goal fGoal;
	private final Variant fInputClause;
	private final Map<Variable, ? extends Term> fMGU;

	private Resolution(Goal goal, Variant inputClause, Map<Variable, ? extends Term> mgu) {
		fGoal = goal;
		fInputClause = inputClause;
		fMGU = mgu;
	}

	static Pair<Goal, Resolution> resolve(Goal goal, Clause inputClause, SLDBranch cutPoint) {
		var variant = Variant.create(inputClause, goal.toClause());
		return resolve(goal, variant, cutPoint);
	}

	private static Pair<Goal, Resolution> resolve(
			Goal goal, Variant inputClause, SLDBranch cutPoint) {
		var subGoal = goal.toSubGoals().head();
		var inputHead = inputClause.getInstance().head();

		var mgu = Unification.getMGU(subGoal, inputHead);
		if (mgu == null) return null;

		var unified = Instances.getInstance(inputClause.getInstance(), mgu);
		var resolvent = goal.nextGoal(unified, mgu, cutPoint);
		return Pair.newPair(resolvent, new Resolution(goal, inputClause, mgu));
	}

	static Pair<Goal, Resolution> resolveSpecial(Goal goal, SLDBranch cutPoint) {
		var subGoal = goal.peek().subGoal();
		var eval = subGoal.eval();

		if (eval == null) return null;

		var resolvent = goal.nextGoal(eval.getResolvent(), eval.getMGU(), cutPoint);
		var dummy = Variant.create(Clause.query(subGoal), goal.toClause());

		return Pair.newPair(resolvent, new Resolution(goal, dummy, eval.getMGU()));
	}

	static Pair<Goal, Resolution> resolveCut(Goal goal) {
		assert goal.peek().subGoal().isCut();

		var v = Variant.create(Clause.fact(AtomicFormula.CUT), goal.toClause());
		Map<Variable, ? extends Term> mgu = Collections.emptyMap();
		var r = new Resolution(goal, v, mgu);
		var resolvent = goal.pop().get2nd();

		return Pair.newPair(resolvent, r);
	}

	Goal getGoal() {
		return fGoal;
	}

	Variant getInputClause() {
		return fInputClause;
	}

	Map<Variable, ? extends Term> getMGU() {
		return fMGU;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Resolution rhs)) return false;

		return Objects.equals(fGoal, rhs.fGoal)
				&& Objects.equals(fInputClause, rhs.fInputClause)
				&& Objects.equals(fMGU, rhs.fMGU);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fGoal)
				+ Objects.hashCode(fInputClause)
				+ Objects.hashCode(fMGU);
	}

	@Override
	public String toString() {
		return "Resolution(goal=%s, inputClause=%s, mgu=%s)".formatted(fGoal, fInputClause, fMGU);
	}
}
