/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

/**
 *
 * @author Naoshi Higuchi
 */
final class Instances {
	static AtomicFormula getInstance(AtomicFormula literal, Pair<Variable, ? extends Term> subst) {
		Map<Variable, Term> s = new HashMap<Variable, Term>();
		s.put(subst.get1st(), subst.get2nd());

		return getInstance(literal, s);
	}

	static Clause getInstance(Clause clause, Map<Variable, ? extends Term> subst) {
		assert (clause != null && subst != null);

		AtomicFormula head = clause.head();
		FList<AtomicFormula> body = clause.body();

		return Clause.clause(
				getInstance(head, subst),
				getInstanceOfLiterals(body, subst));
	}

	static AtomicFormula getInstance(AtomicFormula literal, Map<Variable, ? extends Term> subst) {
		assert (subst != null);
		if (literal == null) return null;

		return AtomicFormula.create(
				literal.predicate(),
				getInstance(literal.args(), subst));
	}

	static FList<AtomicFormula> getInstanceOfLiterals(FList<AtomicFormula> lits, Map<Variable, ? extends Term> subst) {
		assert (lits != null && subst != null);

		if (lits.isEmpty()) return lits;

		AtomicFormula lit = lits.head();
		FList<AtomicFormula> rest = lits.tail();

		return FList.cons(
				getInstance(lit, subst),
				getInstanceOfLiterals(rest, subst));
	}

	static FList<? extends Term> getInstance(FList<? extends Term> terms, Map<Variable, ? extends Term> subst) {
		assert (terms != null && subst != null);

		if (terms.isEmpty()) return terms;

		Term t = terms.head();
		FList<? extends Term> rest = terms.tail();

		if (t instanceof Variable) {
			Variable v = (Variable) t;
			t = getInstance(v, subst);
		}
		if (t instanceof CompoundTerm) {
			CompoundTerm ct = (CompoundTerm) t;
			t = CompoundTerm.create(
					ct.functor(),
					getInstance(ct.args(), subst));
		}

		return FList.cons(t, getInstance(rest, subst));
	}

	static Term getInstance(Variable var, Map<Variable, ? extends Term> subst) {
		assert (var != null && subst != null);

		Term res = var;

		if (subst.containsKey(var)) {
			res = subst.get(var);
		}

		return res;
	}

	static Term getInstance(Variable var, Pair<Variable, ? extends Term> subst) {
		assert (var != null && subst != null);

		Term res = var;

		if (subst.get1st().equals(var)) {
			res = subst.get2nd();
		}

		return res;
	}
}
