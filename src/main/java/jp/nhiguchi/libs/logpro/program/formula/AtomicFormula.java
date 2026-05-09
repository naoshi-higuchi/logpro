package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

import jp.nhiguchi.libs.logpro.program.term.*;

public final class AtomicFormula {
	private final Predicate fPredicate;
	private final FList<? extends Term> fArgs;
	public static final AtomicFormula CUT;

	static {
		CUT = new AtomicFormula(Predicate.CUT, FList.flist());
	}

	private AtomicFormula(Predicate predicate, FList<? extends Term> args) {
		if (predicate == null || args == null) throw new NullPointerException();
		if (predicate.arity() != args.size()) throw new IllegalArgumentException();

		fPredicate = predicate;
		fArgs = args;
	}

	public static AtomicFormula create(Predicate predicate, FList<? extends Term> args) {
		return new AtomicFormula(predicate, args);
	}

	public static AtomicFormula create(Predicate predicate, List<? extends Term> args) {
		return new AtomicFormula(predicate, FList.flist(args));
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
		return fPredicate.type() == Predicate.Type.CUT;
	}

	public boolean isSpecial() {
		return fPredicate.type() == Predicate.Type.SPECIAL;
	}

	public boolean isNormal() {
		return fPredicate.type() == Predicate.Type.NORMAL;
	}

	public Predicate.Evaluable.Eval eval() {
		return fPredicate.eval(fArgs);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof AtomicFormula rhs)) return false;

		return Objects.equals(fPredicate, rhs.fPredicate)
				&& Objects.equals(fArgs, rhs.fArgs);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fPredicate) + Objects.hashCode(fArgs);
	}

	@Override
	public String toString() {
		return "%s(%s)".formatted(fPredicate, fArgs.toStringWithoutBrackets());
	}
}
