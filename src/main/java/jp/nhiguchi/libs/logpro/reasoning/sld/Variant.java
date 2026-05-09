package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

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
		assert goal.isQuery();

		var vars = Util.collectVariables(clause);
		var existingVars = Util.collectVariables(goal);
		var substitute = getSubstitute(vars, existingVars);

		return new Variant(clause, substitute);
	}

	private static Map<Variable, Variable> getSubstitute(
			Set<Variable> vars, Set<Variable> existingVars) {
		var map = new HashMap<Variable, Variable>();

		for (var var_ : vars) {
			if (existingVars.contains(var_)) {
				map.put(var_, rename(var_, existingVars));
			}
		}

		return map;
	}

	private static Variable rename(Variable var, Set<Variable> existingVars) {
		int i = 0;
		Variable renamed;
		do {
			renamed = Variable.create("_%s%d".formatted(var.name(), i++));
		} while (existingVars.contains(renamed));

		return renamed;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Variant rhs)) return false;

		return Objects.equals(fInstance, rhs.fInstance)
				&& Objects.equals(fSubstitute, rhs.fSubstitute);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fInstance) + Objects.hashCode(fSubstitute);
	}

	@Override
	public String toString() {
		return "Variant(instance=%s, substitute=%s)".formatted(fInstance, fSubstitute);
	}
}
