/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

/**
 *
 * @author Naoshi Higuchi
 */
final class Resolution {
	private final Goal fGoal;
	private final Variant fInputClause;
	private final Map<Variable, ? extends Term> fMGU;

	private Resolution(Goal goal, Variant inputClause, Map<Variable, ? extends Term> mgu) {
		fGoal = goal;
		fInputClause = inputClause;
		fMGU = mgu;
	}

	/**
	 * Calculate a resolvent (and a resolution object).
	 *
	 * @param newGoal
	 * @param inputClause
	 * @return retVal.get1st() is the resolvent. retVal.get2nd() is the
	 * resolution object. If resolution failed, returns null.
	 */
	static Pair<Goal, Resolution> resolve(
			Goal goal, Clause inputClause, SLDBranch cutPoint) {
		Variant variant = Variant.create(inputClause, goal.toClause());
		return resolve(goal, variant, cutPoint);
	}

	private static Pair<Goal, Resolution> resolve(
			Goal goal, Variant inputClause, SLDBranch cutPoint) {
		AtomicFormula subGoal = goal.toSubGoals().head();
		AtomicFormula inputHead = inputClause.getInstance().head();

		Map<Variable, ? extends Term> mgu = Unification.getMGU(subGoal, inputHead);
		if (mgu == null) return null;

		Clause unified = Instances.getInstance(
				inputClause.getInstance(), mgu);

		Goal resolvent = getResolvent(unified, mgu, goal, cutPoint);
		return Pair.newPair(resolvent, new Resolution(goal, inputClause, mgu));
	}

	static Pair<Goal, Resolution> resolveSpecial(
			Goal goal, SLDBranch cutPoint) {
		AtomicFormula subGoal = goal.peek().getSubGoal();
		Predicate.Evaluable.Eval eval = subGoal.eval();

		if (eval == null) return null;

		Goal resolvent = getResolvent(
				eval.getResolvent(), eval.getMGU(), goal, cutPoint);

		Variant dummy = Variant.create(
				Clause.query(subGoal), goal.toClause());

		return Pair.newPair(
				resolvent, new Resolution(goal, dummy, eval.getMGU()));
	}

	private static Goal getResolvent(
			Clause unified, Map<Variable, ? extends Term> mgu, Goal oldGoal,
			SLDBranch cutPoint) {
		return oldGoal.nextGoal(unified, mgu, cutPoint);
	}

	static Pair<Goal, Resolution> resolveCut(Goal goal) {
		assert (goal.peek().getSubGoal().isCut());

		Variant v = Variant.create(Clause.fact(AtomicFormula.CUT), goal.toClause());
		Map<Variable, ? extends Term> mgu = Collections.emptyMap();
		Resolution r = new Resolution(goal, v, mgu);

		Goal resolvent = goal.pop().get2nd();

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
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Resolution)) return false;

		Resolution rhs = (Resolution) obj;

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
		return String.format(
				"Resolution(goal=%s, inputClause=%s, mgu=%s)",
				fGoal, fInputClause, fMGU);
	}
}
