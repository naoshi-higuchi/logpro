package jp.nhiguchi.libs.logpro.program.term;

public record Atom(Object value) implements Constant {
	public Atom {
		if (value == null) throw new IllegalArgumentException();
	}

	public static Atom create(Object value) {
		return new Atom(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
