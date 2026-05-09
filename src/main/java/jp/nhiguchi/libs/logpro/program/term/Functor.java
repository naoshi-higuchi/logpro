package jp.nhiguchi.libs.logpro.program.term;

public record Functor(String name, int arity) {
	public Functor {
		if (name == null || arity < 0) throw new IllegalArgumentException();
	}

	public static Functor create(String name, int arity) {
		return new Functor(name, arity);
	}

	@Override
	public String toString() {
		return "%s^%d".formatted(name, arity);
	}
}
