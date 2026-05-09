package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

/**
 * An immutable clause in a logic program — a fact, a rule, or a query.
 *
 * <ul>
 *   <li><strong>Fact:</strong> head only, empty body — e.g., {@code parent(ann, bob).}</li>
 *   <li><strong>Rule:</strong> head and non-empty body — e.g., {@code mother(X, Y) :- parent(X, Y), female(X).}</li>
 *   <li><strong>Query:</strong> no head, non-empty body — e.g., {@code ?- mother(X, cate).}</li>
 * </ul>
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

	/**
	 * Creates a rule or fact clause from a head and a persistent body list.
	 *
	 * @param head the head formula; must not be {@code null}
	 * @param body the body literals
	 * @return a new {@code Clause}
	 * @throws NullPointerException if {@code body} is {@code null}
	 */
	public static Clause clause(AtomicFormula head, FList<AtomicFormula> body) {
		return new Clause(head, body);
	}

	/**
	 * Creates a rule or fact clause from a head and a standard {@link List} body.
	 *
	 * @param head the head formula; must not be {@code null}
	 * @param body the body literals
	 * @return a new {@code Clause}
	 * @throws NullPointerException if {@code body} is {@code null}
	 */
	public static Clause clause(AtomicFormula head, List<AtomicFormula> body) {
		return new Clause(head, FList.flist(body));
	}

	/**
	 * Creates a rule or fact clause from a head and varargs body literals.
	 *
	 * @param head the head formula; must not be {@code null}
	 * @param body the body literals
	 * @return a new {@code Clause}
	 */
	public static Clause clause(AtomicFormula head, AtomicFormula... body) {
		return new Clause(head, FList.flist(body));
	}

	/**
	 * Creates a fact clause with an empty body.
	 *
	 * @param head the head formula; must not be {@code null}
	 * @return a new fact {@code Clause}
	 */
	public static Clause fact(AtomicFormula head) {
		return new Clause(head, EMPTY_BODY);
	}

	/**
	 * Creates a query clause (no head) from a persistent body list.
	 *
	 * @param body the query goals
	 * @return a new query {@code Clause}
	 * @throws NullPointerException if {@code body} is {@code null}
	 */
	public static Clause query(FList<AtomicFormula> body) {
		return new Clause(null, body);
	}

	/**
	 * Creates a query clause (no head) from a standard {@link List} body.
	 *
	 * @param body the query goals
	 * @return a new query {@code Clause}
	 * @throws NullPointerException if {@code body} is {@code null}
	 */
	public static Clause query(List<AtomicFormula> body) {
		return new Clause(null, FList.flist(body));
	}

	/**
	 * Creates a query clause (no head) from varargs body literals.
	 *
	 * @param body the query goals
	 * @return a new query {@code Clause}
	 */
	public static Clause query(AtomicFormula... body) {
		return new Clause(null, FList.flist(body));
	}

	/**
	 * Returns the shared empty-query singleton ({@code ?- .}).
	 *
	 * @return the empty query clause
	 */
	public static Clause emptyQuery() {
		return EMPTY_QUERY;
	}

	/**
	 * Returns the head formula of this clause, or {@code null} if this is a query.
	 *
	 * @return the head formula, or {@code null}
	 */
	public AtomicFormula head() {
		return fHead;
	}

	/**
	 * Returns the body of this clause as an immutable list of literals.
	 * Empty for facts.
	 *
	 * @return the body literal list
	 */
	public FList<AtomicFormula> body() {
		return fBody;
	}

	/**
	 * Returns {@code true} if this clause is a fact (has a head and an empty body).
	 *
	 * @return {@code true} if this is a fact
	 */
	public boolean isFact() {
		return (fHead != null) && (fBody.isEmpty());
	}

	/**
	 * Returns {@code true} if this clause is a query (has no head).
	 *
	 * @return {@code true} if this is a query
	 */
	public boolean isQuery() {
		return fHead == null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Clause rhs)) return false;

		return Objects.equals(fHead, rhs.fHead)
				&& Objects.equals(fBody, rhs.fBody);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fHead) + Objects.hashCode(fBody);
	}

	@Override
	public String toString() {
		if (fHead == null) return "?- %s.".formatted(fBody.toStringWithoutBrackets());
		if (fBody.isEmpty()) return "%s.".formatted(fHead);
		return "%s :- %s.".formatted(fHead, fBody.toStringWithoutBrackets());
	}
}
