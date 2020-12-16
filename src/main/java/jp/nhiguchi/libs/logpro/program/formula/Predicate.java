/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.term.*;

/**
 *
 * @author Naoshi Higuchi
 */
public final class Predicate {
	public static enum Type {
		NORMAL, SPECIAL, CUT
	}

	/**
	 * @param args
	 * @return a Pair of resolvent and MGU. null if failed.
	 */
	public static interface Evaluable {
		public static interface Eval {
			public Clause getResolvent();

			public Map<Variable, ? extends Term> getMGU();
		}

		public Eval eval(List<? extends Term> args);
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

	/**
	 * @param args
	 * @return a Pair of next goal and MGU. null if failed.
	 * @throws UnsupportedOperationException if predicate's type is not SPECIAL.
	 */
	Evaluable.Eval eval(List<? extends Term> args) {
		if (fEvaluable == null) {
			throw new UnsupportedOperationException(
					"eval() is only supported by a SPECIAL predicate.");
		}

		return fEvaluable.eval(args);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Predicate)) return false;

		Predicate rhs = (Predicate) obj;

		return Objects.equals(fName, rhs.fName)
				&& Objects.equals(fArity, rhs.fArity)
				&& Objects.equals(fType, rhs.fType)
				&& Objects.equals(fEvaluable, rhs.fEvaluable);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fName)
				+ Objects.hashCode(fArity)
				+ Objects.hashCode(fType)
				+ Objects.hashCode(fEvaluable);
	}

	@Override
	public String toString() {
		return String.format("%s^%d", fName, fArity);
	}
}
