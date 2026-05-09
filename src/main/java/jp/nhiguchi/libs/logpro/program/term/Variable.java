package jp.nhiguchi.libs.logpro.program.term;

/**
 * An immutable logical variable identified by name.
 *
 * <p>By convention, variable names start with an uppercase letter or underscore
 * (e.g., {@code X}, {@code _Y}). Two variables with the same name are equal.
 *
 * @param name the variable name; must not be {@code null}
 */
public record Variable(String name) implements Term {
	/**
	 * Validates that {@code name} is non-null.
	 *
	 * @throws IllegalArgumentException if {@code name} is {@code null}
	 */
	public Variable {
		if (name == null) throw new IllegalArgumentException();
	}

	/**
	 * Creates a {@code Variable} with the given name.
	 *
	 * @param name the variable name; must not be {@code null}
	 * @return a new {@code Variable}
	 * @throws IllegalArgumentException if {@code name} is {@code null}
	 */
	public static Variable create(String name) {
		return new Variable(name);
	}

	/**
	 * Returns the variable name.
	 *
	 * @return the variable name string
	 */
	@Override
	public String toString() {
		return name;
	}
}
