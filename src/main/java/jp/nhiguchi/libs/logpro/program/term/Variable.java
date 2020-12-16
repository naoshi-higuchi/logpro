/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.term;

/**
 * Immutable.
 *
 * @author Naoshi Higuchi
 */
public final class Variable implements Term {
	private final String fName;

	private Variable(String name) {
		fName = name;
	}

	public static Variable create(String name) {
		if (name == null) throw new IllegalArgumentException();

		return new Variable(name);
	}

	public String name() {
		return fName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Variable)) return false;

		Variable rhs = (Variable) obj;

		return fName.equals(rhs.fName);
	}

	@Override
	public int hashCode() {
		return fName.hashCode();
	}

	@Override
	public String toString() {
		return fName;
	}
}
