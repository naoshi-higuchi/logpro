/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.Objects;
import jp.nhiguchi.libs.flist.*;

import jp.nhiguchi.libs.logpro.program.formula.*;

/**
 *
 * @author Naoshi Higuchi
 */
final class SLDBranch {
	private final Goal fGoal;
	private final Stack fStack;
	private final FList<Clause> fInputClauses;
	public static final SLDBranch NULL = new SLDBranch(null, null, null);

	private SLDBranch(Goal goal, Stack stack, FList<Clause> inputClauses) {
		fGoal = goal;
		fStack = stack;
		fInputClauses = inputClauses;
	}

	static SLDBranch newSLDBranch(Goal goal, Stack stack, FList<Clause> inputClauses) {
		if (goal == null || stack == null) {
			throw new IllegalArgumentException();
			// inputClauses may be null.
		}

		return new SLDBranch(goal, stack, inputClauses);
	}

	Goal getGoal() {
		return fGoal;
	}

	Stack getStack() {
		return fStack;
	}

	FList<Clause> getInputClauses() {
		return fInputClauses;
	}

	boolean isSetUpInputClauses() {
		return fInputClauses != null;
	}

	boolean isSucceeded() {
		return fGoal.isEmpty();
	}

	boolean isNULL() {
		return equals(NULL);
	}

	boolean isForkSuppressiveZone() {
		return fGoal.peek().isForkSuppressiveZone();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof SLDBranch)) return false;

		SLDBranch rhs = (SLDBranch) obj;

		return Objects.equals(fGoal, rhs.fGoal)
				&& Objects.equals(fStack, rhs.fStack)
				&& Objects.equals(fInputClauses, fInputClauses);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fGoal)
				+ Objects.hashCode(fStack)
				+ Objects.hashCode(fInputClauses);
	}

	@Override
	public String toString() {
		if (isNULL()) return "SLDBranch.NULL";

		return String.format(
				"SLDBranch(\ngoal=%s,\nstack=%s,\ninputClauses=%s)",
				fGoal, fStack, fInputClauses);
	}
}
