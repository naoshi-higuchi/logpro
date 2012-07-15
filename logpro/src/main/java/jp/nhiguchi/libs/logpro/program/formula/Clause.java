/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

/**
 * Immutable.
 *
 * @author Naoshi Higuchi
 */
public final class Clause {
	private final AtomicFormula fHead;
	private final FList<AtomicFormula> fBody;
	private static final FList<AtomicFormula> EMPTY_BODY = FList.flist();
	private static final Clause EMPTY_QUERY = query(EMPTY_BODY);

	private Clause(AtomicFormula head, FList<AtomicFormula> body) {
		if (body == null) throw new NullPointerException();

		fHead = head;
		fBody = body;
	}

	public static Clause clause(AtomicFormula head, FList<AtomicFormula> body) {
		return new Clause(head, body);
	}

	public static Clause clause(AtomicFormula head, List<AtomicFormula> body) {
		FList<AtomicFormula> fbody = FList.flist(body);
		return new Clause(head, fbody);
	}

	public static Clause clause(AtomicFormula head, AtomicFormula... body) {
		return new Clause(head, FList.flist(body));
	}

	public static Clause fact(AtomicFormula head) {
		return new Clause(head, EMPTY_BODY);
	}

	public static Clause query(FList<AtomicFormula> body) {
		return new Clause(null, body);
	}

	public static Clause query(List<AtomicFormula> body) {
		FList<AtomicFormula> fbody = FList.flist(body);
		return new Clause(null, fbody);
	}

	public static Clause query(AtomicFormula... body) {
		return new Clause(null, FList.flist(body));
	}

	public static Clause emptyQuery() {
		return EMPTY_QUERY;
	}

	public AtomicFormula head() {
		return fHead;
	}

	public FList<AtomicFormula> body() {
		return fBody;
	}

	public boolean isFact() {
		return (fHead != null) && (fBody.isEmpty());
	}

	public boolean isQuery() {
		return fHead == null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Clause)) return false;

		Clause rhs = (Clause) obj;

		return Objects.equals(fHead, rhs.fHead)
				&& Objects.equals(fBody, rhs.fBody);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fHead)
				+ Objects.hashCode(fBody);
	}

	@Override
	public String toString() {
		if (fHead == null)
			return String.format("?- %s.", fBody.toStringWithoutBrackets());

		if (fBody.isEmpty()) return String.format("%s.", fHead);

		return String.format("%s :- %s.", fHead, fBody.toStringWithoutBrackets());
	}
}
