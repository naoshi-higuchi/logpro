/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

/**
 * Immutable.
 *
 * @author Naoshi Higuchi
 */
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
		List<Clause> res = fMap.get(literal.predicate());

		if (res == null) return Collections.emptyList();

		return res;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof SimpleProgram)) return false;

		SimpleProgram rhs = (SimpleProgram) obj;
		return fMap.equals(rhs.fMap);
	}

	@Override
	public int hashCode() {
		return fMap.hashCode();
	}
}
