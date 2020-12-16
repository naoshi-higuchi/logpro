/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

/**
 *
 * @author Naoshi Higuchi
 */
public final class SimpleProgramBuilder {
	private static class ComparatorImpl implements Comparator<Clause> {
		private ComparatorImpl() {
		}

		@Override
		public int compare(Clause o1, Clause o2) {
			assert (o1.head().predicate().equals(
					o2.head().predicate()));

			FList<AtomicFormula> b1 = o1.body();
			FList<AtomicFormula> b2 = o2.body();

			int len1, len2;

			len1 = (b1 == null) ? 0 : b1.size();
			len2 = (b2 == null) ? 0 : b2.size();

			int c = len1 - len2;
			if (c != 0) return c;

			return o2.hashCode() - o1.hashCode();
		}
	}
	private Map<Predicate, LinkedList<Clause>> fMap;

	public SimpleProgramBuilder() {
		fMap = new HashMap<Predicate, LinkedList<Clause>>();
	}

	public SimpleProgramBuilder add(Clause clause) {
		if (clause == null) throw new NullPointerException();
		AtomicFormula head = clause.head();

		if (head == null)
			throw new IllegalArgumentException("`clause' must not be a query.");
		Predicate pred = clause.head().predicate();

		LinkedList<Clause> clauses = fMap.get(pred);
		if (clauses == null) clauses = new LinkedList();

		if (clauses.contains(clause)) return this;
		clauses.add(clause);
		fMap.put(pred, clauses);
		return this;
	}

	public SimpleProgramBuilder addAll(Collection<Clause> clauses) {
		for (Clause clause : clauses) {
			add(clause);
		}
		return this;
	}

	public Program toProgram() {
		Map<Predicate, FList<Clause>> map = new HashMap<Predicate, FList<Clause>>();

		for (Map.Entry<Predicate, LinkedList<Clause>> ent : fMap.entrySet()) {
			Predicate pred = ent.getKey();
			LinkedList<Clause> clauses = ent.getValue(); // clauses must be sortable.
			if (!containsCut(clauses)) {
				Collections.sort(clauses, new ComparatorImpl());
			}
			FList<Clause> fcls = FList.flist(clauses);
			map.put(pred, fcls);
		}

		return SimpleProgram.create(map);
	}

	private static boolean containsCut(List<Clause> clauses) {
		for (Clause clause : clauses) {
			if (clause.body().contains(AtomicFormula.CUT)) return true;
		}
		return false;
	}
}
