package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

import jp.nhiguchi.libs.logpro.program.term.*;

/**
 * An immutable atomic formula: a {@link Predicate} applied to zero or more {@link Term}s.
 *
 * <p>Examples: {@code parent(ann, bob)}, {@code female(X)}, the cut literal {@code !}.
 * The predicate's arity must equal the number of argument terms.
 */
public final class AtomicFormula {
	private final Predicate fPredicate;
	private final FList<? extends Term> fArgs;

	/** The built-in cut literal ({@code !}). */
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

	/**
	 * Creates an {@code AtomicFormula} from a predicate and a persistent argument list.
	 *
	 * @param predicate the predicate; must not be {@code null}
	 * @param args      the argument list; size must equal {@code predicate.arity()}
	 * @return a new {@code AtomicFormula}
	 * @throws NullPointerException     if {@code predicate} or {@code args} is {@code null}
	 * @throws IllegalArgumentException if argument count does not match predicate arity
	 */
	public static AtomicFormula create(Predicate predicate, FList<? extends Term> args) {
		return new AtomicFormula(predicate, args);
	}

	/**
	 * Creates an {@code AtomicFormula} from a predicate and a standard {@link List} of arguments.
	 *
	 * @param predicate the predicate; must not be {@code null}
	 * @param args      the argument list; size must equal {@code predicate.arity()}
	 * @return a new {@code AtomicFormula}
	 * @throws NullPointerException     if {@code predicate} or {@code args} is {@code null}
	 * @throws IllegalArgumentException if argument count does not match predicate arity
	 */
	public static AtomicFormula create(Predicate predicate, List<? extends Term> args) {
		return new AtomicFormula(predicate, FList.flist(args));
	}

	/**
	 * Creates an {@code AtomicFormula} from a predicate and a varargs array of arguments.
	 *
	 * @param predicate the predicate; must not be {@code null}
	 * @param args      the arguments; count must equal {@code predicate.arity()}
	 * @return a new {@code AtomicFormula}
	 * @throws NullPointerException     if {@code predicate} is {@code null}
	 * @throws IllegalArgumentException if argument count does not match predicate arity
	 */
	public static AtomicFormula create(Predicate predicate, Term... args) {
		return new AtomicFormula(predicate, FList.flist(args));
	}

	/**
	 * Returns the predicate of this formula.
	 *
	 * @return the predicate
	 */
	public Predicate predicate() {
		return fPredicate;
	}

	/**
	 * Returns the argument list of this formula.
	 *
	 * @return an immutable list of argument terms
	 */
	public FList<? extends Term> args() {
		return fArgs;
	}

	/**
	 * Returns {@code true} if this formula is the cut literal ({@code !}).
	 *
	 * @return {@code true} if this is a cut literal
	 */
	public boolean isCut() {
		return fPredicate.type() == Predicate.Type.CUT;
	}

	/**
	 * Returns {@code true} if this formula uses a special (built-in) predicate.
	 *
	 * @return {@code true} if the predicate type is {@link Predicate.Type#SPECIAL}
	 */
	public boolean isSpecial() {
		return fPredicate.type() == Predicate.Type.SPECIAL;
	}

	/**
	 * Returns {@code true} if this formula uses a normal program predicate.
	 *
	 * @return {@code true} if the predicate type is {@link Predicate.Type#NORMAL}
	 */
	public boolean isNormal() {
		return fPredicate.type() == Predicate.Type.NORMAL;
	}

	/**
	 * Evaluates this formula using its special predicate's {@link Predicate.Evaluable}.
	 *
	 * @return an {@link Predicate.Evaluable.Eval} on success, or {@code null} if evaluation fails
	 * @throws UnsupportedOperationException if the predicate is not of type
	 *         {@link Predicate.Type#SPECIAL}
	 */
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
