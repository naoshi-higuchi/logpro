package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

final class Util {
	static AtomicFormula getSubGoal(Clause goal) {
		if (goal.body().isEmpty()) throw new IllegalArgumentException();
		return goal.body().head();
	}

	static Set<Variable> collectVariables(Clause clause) {
		var head = clause.head();
		var body = clause.body();

		var vars = new HashSet<Variable>();
		if (head != null) collectVariables(head, vars);
		collectVariables(body, vars);

		return vars;
	}

	private static void collectVariables(FList<AtomicFormula> literals, Set<Variable> vars) {
		for (var lit : literals) collectVariables(lit, vars);
	}

	private static void collectVariables(AtomicFormula literal, Set<Variable> vars) {
		assert literal != null;
		collectVars(literal.args(), vars);
	}

	private static void collectVars(FList<? extends Term> terms, Set<Variable> vars) {
		assert terms != null;

		for (var t : terms) {
			switch (t) {
				case Variable v -> vars.add(v);
				case CompoundTerm ct -> collectVars(ct.args(), vars);
				case Atom _ -> {}
			}
		}
	}

	static boolean isAlphaEquivalent(Term t1, Term t2) {
		return collectRenamings(t1, t2) != null;
	}

	static boolean isAlphaEquivalent(AtomicFormula lit1, AtomicFormula lit2) {
		if (!lit1.predicate().equals(lit2.predicate())) return false;

		var renameMap = new HashMap<Variable, Variable>();
		var args1 = lit1.args();
		var args2 = lit2.args();

		for (var t1 : args1) {
			var t2 = args2.head();
			args2 = args2.tail();

			var m = collectRenamings(t1, t2);
			if (m == null) return false;

			for (var ent : m.entrySet()) {
				if (renameMap.put(ent.getKey(), ent.getValue()) != null) return false;
			}
		}

		var dupChecker = new HashSet<Variable>();
		for (var v : renameMap.values()) {
			if (!dupChecker.add(v)) return false;
		}

		return true;
	}

	private static Map<Variable, Variable> collectRenamings(Term t1, Term t2) {
		var renameMap = new HashMap<Variable, Variable>();

		if (t1.equals(t2)) return renameMap;

		if (t1 instanceof Variable v1 && t2 instanceof Variable v2) {
			renameMap.put(v1, v2);
			return renameMap;
		}

		if (t1 instanceof CompoundTerm ct1 && t2 instanceof CompoundTerm ct2) {
			var args1 = ct1.args();
			var args2 = ct2.args();

			for (var a1 : args1) {
				var a2 = args2.head();
				args2 = args2.tail();

				var m = collectRenamings(a1, a2);
				if (m == null) return null;

				for (var ent : m.entrySet()) {
					if (renameMap.put(ent.getKey(), ent.getValue()) != null) return null;
				}
			}

			return renameMap;
		}

		return null;
	}
}
