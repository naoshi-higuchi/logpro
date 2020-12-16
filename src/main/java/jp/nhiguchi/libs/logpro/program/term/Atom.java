/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.term;

/**
 * Immutable iff it's value-object is immutable.
 *
 * @author Naoshi Higuchi
 */
public final class Atom implements Constant {
	private final Object fValue;

	private Atom(Object value) {
		fValue = value;
	}

	/**
	 *
	 * @param value should be immutable. If the value is mutated, solver does
	 * NOT work correctly.
	 * @return
	 */
	public static Atom create(Object value) {
		if (value == null) throw new IllegalArgumentException();

		return new Atom(value);
	}

	public Object value() {
		return fValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Atom)) return false;

		Atom rhs = (Atom) obj;

		return fValue.equals(rhs.fValue);
	}

	@Override
	public int hashCode() {
		return fValue.hashCode();
	}

	@Override
	public String toString() {
		return fValue.toString();
	}
}
