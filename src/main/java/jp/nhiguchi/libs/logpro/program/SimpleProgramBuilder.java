package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

/**
 * A fluent builder for constructing an immutable {@link Program} from a set of clauses.
 *
 * <p>Clauses are deduplicated automatically. For each predicate, clauses are sorted
 * by body length (shorter bodies first) unless any clause contains a cut ({@code !}),
 * in which case the original insertion order is preserved.
 *
 * <p>Example usage:
 * <pre>{@code
 * Program p = new SimpleProgramBuilder()
 *     .add(SimpleParser.fact("parent(ann, bob)."))
 *     .add(SimpleParser.clause("mother(X, Y) :- parent(X, Y), female(X)."))
 *     .toProgram();
 * }</pre>
 */
public final class SimpleProgramBuilder {
	private final Map<Predicate, LinkedList<Clause>> fMap = new HashMap<>();

	/**
	 * Adds a clause to this builder.
	 *
	 * <p>Duplicate clauses (by {@link Clause#equals}) are silently ignored.
	 *
	 * @param clause the clause to add; must not be {@code null} and must not be a query
	 * @return this builder, for chaining
	 * @throws NullPointerException     if {@code clause} is {@code null}
	 * @throws IllegalArgumentException if {@code clause} is a query (has no head)
	 */
	public SimpleProgramBuilder add(Clause clause) {
		if (clause == null) throw new NullPointerException();

		var head = clause.head();
		if (head == null) throw new IllegalArgumentException("`clause' must not be a query.");

		var clauses = fMap.computeIfAbsent(head.predicate(), _ -> new LinkedList<>());
		if (!clauses.contains(clause)) clauses.add(clause);

		return this;
	}

	/**
	 * Adds all clauses in the given collection to this builder.
	 *
	 * @param clauses the clauses to add; must not be {@code null}
	 * @return this builder, for chaining
	 * @see #add(Clause)
	 */
	public SimpleProgramBuilder addAll(Collection<Clause> clauses) {
		for (var clause : clauses) add(clause);
		return this;
	}

	/**
	 * Builds and returns an immutable {@link Program} from the clauses added so far.
	 *
	 * <p>For each predicate, clauses that do not contain a cut are sorted by body
	 * length (ascending). Clauses containing cut are left in insertion order.
	 *
	 * @return a new immutable {@code Program}
	 */
	public Program toProgram() {
		var map = new HashMap<Predicate, FList<Clause>>();

		for (var ent : fMap.entrySet()) {
			var clauses = ent.getValue();
			if (!containsCut(clauses)) {
				clauses.sort((o1, o2) -> {
					var b1 = o1.body();
					var b2 = o2.body();
					int len1 = (b1 == null) ? 0 : b1.size();
					int len2 = (b2 == null) ? 0 : b2.size();
					int c = len1 - len2;
					return (c != 0) ? c : o2.hashCode() - o1.hashCode();
				});
			}
			map.put(ent.getKey(), FList.flist(clauses));
		}

		return SimpleProgram.create(map);
	}

	private static boolean containsCut(List<Clause> clauses) {
		for (var clause : clauses) {
			if (clause.body().contains(AtomicFormula.CUT)) return true;
		}
		return false;
	}
}
