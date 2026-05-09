package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.formula.*;

/**
 * A logic program: a collection of {@link Clause}s indexed by predicate.
 *
 * <p>The program serves as the knowledge base for SLD resolution.
 * Given an atomic formula (literal), it returns the list of clauses
 * whose heads match that literal's predicate.
 */
public interface Program {
	/**
	 * Returns all clauses in this program whose head predicate matches the
	 * predicate of the given literal.
	 *
	 * @param literal the query literal used to select clauses; must not be {@code null}
	 * @return a list of matching clauses; empty if none are found
	 */
	List<Clause> clauses(AtomicFormula literal);
}
