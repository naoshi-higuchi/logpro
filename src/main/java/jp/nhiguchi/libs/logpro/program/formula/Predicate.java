package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.term.*;

/**
 * An immutable predicate symbol with a name, arity, and type.
 *
 * <p>Predicates come in three kinds defined by {@link Type}:
 * <ul>
 *   <li>{@link Type#NORMAL} — resolved against program clauses via SLD resolution</li>
 *   <li>{@link Type#SPECIAL} — evaluated directly by a custom {@link Evaluable}</li>
 *   <li>{@link Type#CUT} — the Prolog cut operator ({@code !})</li>
 * </ul>
 *
 * <p>Two predicates are equal when their name, arity, type, and evaluable are all equal.
 */
public final class Predicate {
	/**
	 * The kind of a predicate, determining how it is resolved during SLD resolution.
	 */
	public enum Type {
		/** Resolved against program clauses in the normal SLD fashion. */
		NORMAL,
		/** Evaluated directly by a custom {@link Evaluable} function. */
		SPECIAL,
		/** The Prolog cut operator ({@code !}). */
		CUT
	}

	/**
	 * Functional interface for evaluating a special predicate.
	 *
	 * <p>Implementations compute a resolvent clause and an MGU for a given argument list,
	 * or return {@code null} if evaluation fails.
	 */
	@FunctionalInterface
	public interface Evaluable {
		/**
		 * The result of a successful special-predicate evaluation, containing the
		 * resolvent clause and the most general unifier.
		 */
		interface Eval {
			/**
			 * Returns the resolvent clause produced by this evaluation step.
			 *
			 * @return the resolvent clause
			 */
			Clause getResolvent();

			/**
			 * Returns the most general unifier (MGU) produced by this evaluation step.
			 *
			 * @return a map from variables to their bound terms
			 */
			Map<Variable, ? extends Term> getMGU();
		}

		/**
		 * Evaluates this predicate against the given argument list.
		 *
		 * @param args the argument terms; must not be {@code null}
		 * @return an {@link Eval} containing the resolvent and MGU on success,
		 *         or {@code null} if evaluation fails
		 */
		Eval eval(List<? extends Term> args);
	}

	private final String fName;
	private final int fArity;
	private final Type fType;
	private final Evaluable fEvaluable;

	/** The built-in cut predicate ({@code !}). */
	public static final Predicate CUT = new Predicate("!", 0, Type.CUT, null);

	private Predicate(String name, int arity, Type type, Evaluable evaluable) {
		fName = name;
		fArity = arity;
		fType = type;
		fEvaluable = evaluable;
	}

	/**
	 * Creates a normal predicate resolved against program clauses.
	 *
	 * @param name  the predicate name; must not be {@code null}
	 * @param arity the arity; must be &ge; 0
	 * @return a new normal {@code Predicate}
	 * @throws IllegalArgumentException if {@code name} is {@code null} or {@code arity < 0}
	 */
	public static Predicate create(String name, int arity) {
		if (name == null || arity < 0) throw new IllegalArgumentException();

		return new Predicate(name, arity, Type.NORMAL, null);
	}

	/**
	 * Creates a special predicate evaluated by the given {@link Evaluable}.
	 *
	 * @param name      the predicate name; must not be {@code null}
	 * @param arity     the arity; must be &ge; 0
	 * @param evaluable the evaluation function; must not be {@code null}
	 * @return a new special {@code Predicate}
	 * @throws IllegalArgumentException if any argument is invalid or {@code null}
	 */
	public static Predicate createSpecial(String name, int arity, Evaluable evaluable) {
		if (name == null || arity < 0 || evaluable == null) {
			throw new IllegalArgumentException();
		}

		return new Predicate(name, arity, Type.SPECIAL, evaluable);
	}

	/**
	 * Returns the predicate name.
	 *
	 * @return the predicate name
	 */
	public String name() {
		return fName;
	}

	/**
	 * Returns the predicate arity.
	 *
	 * @return the number of arguments this predicate takes
	 */
	public int arity() {
		return fArity;
	}

	/**
	 * Returns the predicate type.
	 *
	 * @return {@link Type#NORMAL}, {@link Type#SPECIAL}, or {@link Type#CUT}
	 */
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
