package jp.nhiguchi.libs.logpro.program.term;

/**
 * An immutable atomic constant term that wraps an arbitrary value object.
 *
 * <p>Atoms are leaf constants in the term hierarchy, representing
 * indivisible values such as {@code ann} or {@code bob}.
 * Equality is delegated to the wrapped value's {@link Object#equals(Object)} method.
 *
 * <p><strong>Important:</strong> the wrapped value should itself be immutable.
 * Mutating the value after creation leads to undefined solver behaviour.
 *
 * @param value the wrapped value; must not be {@code null}
 */
public record Atom(Object value) implements Constant {
	/**
	 * Validates that {@code value} is non-null.
	 *
	 * @throws IllegalArgumentException if {@code value} is {@code null}
	 */
	public Atom {
		if (value == null) throw new IllegalArgumentException();
	}

	/**
	 * Creates an {@code Atom} wrapping the given value.
	 *
	 * @param value the value to wrap; must not be {@code null}
	 * @return a new {@code Atom} wrapping {@code value}
	 * @throws IllegalArgumentException if {@code value} is {@code null}
	 */
	public static Atom create(Object value) {
		return new Atom(value);
	}

	/**
	 * Returns the string representation of the wrapped value.
	 *
	 * @return {@code value.toString()}
	 */
	@Override
	public String toString() {
		return value.toString();
	}
}
