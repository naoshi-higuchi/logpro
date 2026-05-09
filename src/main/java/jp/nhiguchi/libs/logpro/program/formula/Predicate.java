package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.term.*;

public final class Predicate {
	public enum Type {
		NORMAL, SPECIAL, CUT
	}

	@FunctionalInterface
	public interface Evaluable {
		interface Eval {
			Clause getResolvent();
			Map<Variable, ? extends Term> getMGU();
		}

		Eval eval(List<? extends Term> args);
	}

	private final String fName;
	private final int fArity;
	private final Type fType;
	private final Evaluable fEvaluable;
	public static final Predicate CUT = new Predicate("!", 0, Type.CUT, null);

	private Predicate(String name, int arity, Type type, Evaluable evaluable) {
		fName = name;
		fArity = arity;
		fType = type;
		fEvaluable = evaluable;
	}

	public static Predicate create(String name, int arity) {
		if (name == null || arity < 0) throw new IllegalArgumentException();

		return new Predicate(name, arity, Type.NORMAL, null);
	}

	public static Predicate createSpecial(String name, int arity, Evaluable evaluable) {
		if (name == null || arity < 0 || evaluable == null) {
			throw new IllegalArgumentException();
		}

		return new Predicate(name, arity, Type.SPECIAL, evaluable);
	}

	public String name() {
		return fName;
	}

	public int arity() {
		return fArity;
	}

	public Type type() {
		return fType;
	}

	Evaluable.Eval eval(List<? extends Term> args) {
		if (fEvaluable == null) {
			throw new UnsupportedOperationException(
					"eval() is only supported by a SPECIAL predicate.");
		}

		return fEvaluable.eval(args);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Predicate rhs)) return false;

		return Objects.equals(fName, rhs.fName)
				&& fArity == rhs.fArity
				&& fType == rhs.fType
				&& Objects.equals(fEvaluable, rhs.fEvaluable);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fName)
				+ Integer.hashCode(fArity)
				+ Objects.hashCode(fType)
				+ Objects.hashCode(fEvaluable);
	}

	@Override
	public String toString() {
		return "%s^%d".formatted(fName, fArity);
	}
}
