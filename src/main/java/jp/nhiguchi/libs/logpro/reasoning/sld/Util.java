/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

/**
 *
 * @author Naoshi Higuchi
 */
final class Util {
	static AtomicFormula getSubGoal(Clause goal) {
		if (goal.body().isEmpty()) throw new IllegalArgumentException();

		return goal.body().head();
	}

	static Set<Variable> collectVariables(Clause clause) {
		AtomicFormula head = clause.head();
		FList<AtomicFormula> body = clause.body();

		Set<Variable> vars = new HashSet<Variable>();
		if (head != null) collectVariables(head, vars);
		collectVariables(body, vars);

		return vars;
	}

	private static Set<Variable> collectVariables(FList<AtomicFormula> literals, Set<Variable> vars) {
		for (AtomicFormula lit : literals) {
			collectVariables(lit, vars);
		}

		return vars;
	}

	private static void collectVariables(AtomicFormula literal, Set<Variable> vars) {
		assert (literal != null);

		collectVars(literal.args(), vars);
	}

	private static void collectVars(FList<? extends Term> terms, Set<Variable> vars) {
		assert (terms != null);

		for (Term t : terms) {
			if (t instanceof Variable) {
				Variable v = (Variable) t;
				vars.add(v);
			} else if (t instanceof CompoundTerm) {
				CompoundTerm ct = (CompoundTerm) t;
				collectVars(ct.args(), vars);
			}
		}
	}

	static boolean isAlphaEquivalent(Term t1, Term t2) {
		return collectRenamings(t1, t2) != null;
	}

	static boolean isAlphaEquivalent(AtomicFormula lit1, AtomicFormula lit2) {
		if (!lit1.predicate().equals(lit2.predicate())) return false;

		Map<Variable, Variable> renameMap = new HashMap<Variable, Variable>();
		FList<? extends Term> args1 = lit1.args();
		FList<? extends Term> args2 = lit2.args();

		for (Term t1 : args1) {
			Term t2 = args2.head();
			args2 = args2.tail();

			Map<Variable, Variable> m = collectRenamings(t1, t2);
			if (m == null) return false;

			for (Map.Entry<Variable, Variable> ent : m.entrySet()) {
				if (renameMap.put(ent.getKey(), ent.getValue()) != null)
					return false;
			}
		}

		Set<Variable> dupChecker = new HashSet<Variable>();
		for (Variable v : renameMap.values()) {
			if (dupChecker.add(v) == false) return false;
		}

		return true;
	}

	/**
	 *
	 * @param t1
	 * @param t2
	 * @return If t1 equals t2, empty map. If t1 is alpha equivalent t2,
	 * non-empty map. Others, null.
	 */
	private static Map<Variable, Variable> collectRenamings(Term t1, Term t2) {
		Map<Variable, Variable> renameMap = new HashMap<Variable, Variable>();

		if (t1.equals(t2)) return renameMap; // empty map.

		if (t1 instanceof Variable && t2 instanceof Variable) {
			renameMap.put((Variable) t1, (Variable) t2);
			return renameMap;
		}

		if (t1 instanceof CompoundTerm && t2 instanceof CompoundTerm) {
			CompoundTerm ct1 = (CompoundTerm) t1;
			CompoundTerm ct2 = (CompoundTerm) t2;

			FList<? extends Term> args1 = ct1.args();
			FList<? extends Term> args2 = ct2.args();

			for (Term a1 : args1) {
				Term a2 = args2.head();
				args2 = args2.tail();

				Map<Variable, Variable> m = collectRenamings(a1, a2);
				if (m == null) return null;

				for (Map.Entry<Variable, Variable> ent : m.entrySet()) {
					if (renameMap.put(ent.getKey(), ent.getValue()) != null)
						return null;
				}
			}

			return renameMap;
		}

		return null;
	}
}
