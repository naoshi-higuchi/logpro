package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

final class SimpleProgram implements Program {
	private final Map<Predicate, FList<Clause>> fMap;

	private SimpleProgram(Map<Predicate, FList<Clause>> map) {
		if (map == null) throw new NullPointerException();

		fMap = Collections.unmodifiableMap(map);
	}

	static SimpleProgram create(Map<Predicate, FList<Clause>> map) {
		return new SimpleProgram(map);
	}

	@Override
	public List<Clause> clauses(AtomicFormula literal) {
		return fMap.getOrDefault(literal.predicate(), FList.flist());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof SimpleProgram rhs)) return false;

		return fMap.equals(rhs.fMap);
	}

	@Override
	public int hashCode() {
		return fMap.hashCode();
	}
}
