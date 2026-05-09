package jp.nhiguchi.libs.logpro.program.term;

/**
 * An immutable functor — a predicate-like symbol with a fixed arity used
 * as the head of a {@link CompoundTerm}.
 *
 * <p>A functor is identified by its {@link #name()} and {@link #arity()} together.
 * For example, {@code point/2} is a functor with name {@code "point"} and arity 2.
 *
 * @param name  the functor name; must not be {@code null}
 * @param arity the number of arguments; must be &ge; 0
 */
public record Functor(String name, int arity) {
	/**
	 * Validates that {@code name} is non-null and {@code arity} is non-negative.
	 *
	 * @throws IllegalArgumentException if {@code name} is {@code null} or {@code arity < 0}
	 */
	public Functor {
		if (name == null || arity < 0) throw new IllegalArgumentException();
	}

	/**
	 * Creates a {@code Functor} with the given name and arity.
	 *
	 * @param name  the functor name; must not be {@code null}
	 * @param arity the number of arguments; must be &ge; 0
	 * @return a new {@code Functor}
	 * @throws IllegalArgumentException if {@code name} is {@code null} or {@code arity < 0}
	 */
	public static Functor create(String name, int arity) {
		return new Functor(name, arity);
	}

	/**
	 * Returns a string of the form {@code name^arity}.
	 *
	 * @return the functor in {@code name^arity} notation
	 */
	@Override
	public String toString() {
		return "%s^%d".formatted(name, arity);
	}
}
