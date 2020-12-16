/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.term;

import java.util.Objects;

/**
 * Immutable.
 *
 * @author Naoshi Higuchi
 */
public final class Functor {
	private final String fName;
	private final int fArity;

	private Functor(String name, int arity) {
		fName = name;
		fArity = arity;
	}

	public static Functor create(String name, int arity) {
		if (name == null || arity < 0) throw new IllegalArgumentException();

		return new Functor(name, arity);
	}

	public String name() {
		return fName;
	}

	public int arity() {
		return fArity;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Functor)) return false;

		Functor rhs = (Functor) obj;

		return Objects.equals(fName, rhs.fName)
				&& Objects.equals(fArity, rhs.fArity);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fName)
				+ Objects.hashCode(fArity);
	}

	@Override
	public String toString() {
		return String.format("%s^%d", fName, fArity);
	}
}
