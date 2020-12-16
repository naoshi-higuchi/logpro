/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

/**
 * Immutable.
 *
 * @author Naoshi Higuchi
 */
final class Variant {
	private final Clause fInstance;
	private final Map<Variable, Variable> fSubstitute;

	private Variant(Clause clause, Map<Variable, Variable> substitute) {
		fSubstitute = Collections.unmodifiableMap(substitute);
		fInstance = Instances.getInstance(clause, fSubstitute);
	}

	Clause getInstance() {
		return fInstance;
	}

	Map<Variable, Variable> getSubstitute() {
		return fSubstitute;
	}

	static Variant create(Clause clause, Clause goal) {
		assert (goal.isQuery());

		Set<Variable> vars = Util.collectVariables(clause);
		Set<Variable> existingVars = Util.collectVariables(goal);

		Map<Variable, Variable> substitute = getSubstitute(vars, existingVars);

		return new Variant(clause, substitute);
	}

	private static Map<Variable, Variable> getSubstitute(Set<Variable> vars, Set<Variable> existingVars) {
		Map<Variable, Variable> map = new HashMap<Variable, Variable>();

		for (Variable var : vars) {
			if (existingVars.contains(var)) {
				Variable renamed = rename(var, existingVars);
				map.put(var, renamed);
			}
		}

		return map;
	}

	private static Variable rename(Variable var, Set<Variable> existingVars) {
		Variable renamed;

		int i = 0;
		do {
			String name = String.format("_%s%d", var.name(), i);
			renamed = Variable.create(name);
			++i;
		} while (existingVars.contains(renamed));

		return renamed;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Variant)) return false;

		Variant rhs = (Variant) obj;

		return Objects.equals(fInstance, rhs.fInstance)
				&& Objects.equals(fSubstitute, rhs.fSubstitute);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fInstance)
				+ Objects.hashCode(fSubstitute);
	}

	@Override
	public String toString() {
		return String.format(
				"Variant(instance=%s, substitute=%s)",
				fInstance, fSubstitute);
	}
}
