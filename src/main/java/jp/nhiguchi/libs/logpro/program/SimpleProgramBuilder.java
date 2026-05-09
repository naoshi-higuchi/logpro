package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

public final class SimpleProgramBuilder {
	private final Map<Predicate, LinkedList<Clause>> fMap = new HashMap<>();

	public SimpleProgramBuilder add(Clause clause) {
		if (clause == null) throw new NullPointerException();

		var head = clause.head();
		if (head == null) throw new IllegalArgumentException("`clause' must not be a query.");

		var clauses = fMap.computeIfAbsent(head.predicate(), _ -> new LinkedList<>());
		if (!clauses.contains(clause)) clauses.add(clause);

		return this;
	}

	public SimpleProgramBuilder addAll(Collection<Clause> clauses) {
		for (var clause : clauses) add(clause);
		return this;
	}

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
