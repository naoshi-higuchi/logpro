package jp.nhiguchi.libs.logpro.program.term;

public record Variable(String name) implements Term {
	public Variable {
		if (name == null) throw new IllegalArgumentException();
	}

	public static Variable create(String name) {
		return new Variable(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
