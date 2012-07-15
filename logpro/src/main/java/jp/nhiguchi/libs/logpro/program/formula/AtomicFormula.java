/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

import jp.nhiguchi.libs.logpro.program.term.*;

/**
 * Immutable.
 *
 * @author Naoshi Higuchi
 */
public final class AtomicFormula {
	private final Predicate fPredicate;
	private final FList<? extends Term> fArgs;
	public static final AtomicFormula CUT;

	static {
		FList<? extends Term> args = FList.flist();
		CUT = new AtomicFormula(Predicate.CUT, args);
	}

	private AtomicFormula(Predicate predicate, FList<? extends Term> args) {
		if (predicate == null || args == null) throw new NullPointerException();

		if (predicate.arity() != args.size())
			throw new IllegalArgumentException();

		fPredicate = predicate;
		fArgs = args;
	}

	public static AtomicFormula create(Predicate predicate, FList<? extends Term> args) {
		return new AtomicFormula(predicate, args);
	}

	public static AtomicFormula create(Predicate predicate, List<? extends Term> args) {
		FList<? extends Term> fargs = FList.flist(args);
		return new AtomicFormula(predicate, fargs);
	}

	public static AtomicFormula create(Predicate predicate, Term... args) {
		return new AtomicFormula(predicate, FList.flist(args));
	}

	public Predicate predicate() {
		return fPredicate;
	}

	public FList<? extends Term> args() {
		return fArgs;
	}

	public boolean isCut() {
		return fPredicate.type().equals(Predicate.Type.CUT);
	}

	public boolean isSpecial() {
		return fPredicate.type().equals(Predicate.Type.SPECIAL);
	}

	public boolean isNormal() {
		return fPredicate.type().equals(Predicate.Type.NORMAL);
	}

	/**
	 *
	 * @return An Eval object which contains a resolvent and a MGU. null if
	 * failed.
	 * @throws UnsupportedOperationException if predicate's type is not SPECIAL.
	 */
	public Predicate.Evaluable.Eval eval() {
		return fPredicate.eval(fArgs);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof AtomicFormula)) return false;

		AtomicFormula rhs = (AtomicFormula) obj;

		return Objects.equals(fPredicate, rhs.fPredicate)
				&& Objects.equals(fArgs, rhs.fArgs);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fPredicate)
				+ Objects.hashCode(fArgs);
	}

	@Override
	public String toString() {
		return String.format("%s(%s)",
				fPredicate, fArgs.toStringWithoutBrackets());
	}
}
