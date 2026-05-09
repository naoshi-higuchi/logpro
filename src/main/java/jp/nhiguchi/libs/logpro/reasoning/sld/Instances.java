package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

final class Instances {
	static AtomicFormula getInstance(AtomicFormula literal, Pair<Variable, ? extends Term> subst) {
		var s = new HashMap<Variable, Term>();
		s.put(subst.get1st(), subst.get2nd());
		return getInstance(literal, s);
	}

	static Clause getInstance(Clause clause, Map<Variable, ? extends Term> subst) {
		assert clause != null && subst != null;

		return Clause.clause(
				getInstance(clause.head(), subst),
				getInstanceOfLiterals(clause.body(), subst));
	}

	static AtomicFormula getInstance(AtomicFormula literal, Map<Variable, ? extends Term> subst) {
		assert subst != null;
		if (literal == null) return null;

		return AtomicFormula.create(
				literal.predicate(),
				getInstance(literal.args(), subst));
	}

	static FList<AtomicFormula> getInstanceOfLiterals(
			FList<AtomicFormula> lits, Map<Variable, ? extends Term> subst) {
		assert lits != null && subst != null;

		if (lits.isEmpty()) return lits;

		return FList.cons(
				getInstance(lits.head(), subst),
				getInstanceOfLiterals(lits.tail(), subst));
	}

	// NOTE: sequential ifs are intentional — a Variable may substitute to a CompoundTerm,
	// requiring both transformations on the resulting value.
	static FList<? extends Term> getInstance(
			FList<? extends Term> terms, Map<Variable, ? extends Term> subst) {
		assert terms != null && subst != null;

		if (terms.isEmpty()) return terms;

		Term t = terms.head();
		var rest = terms.tail();

		if (t instanceof Variable v) {
			t = getInstance(v, subst);
		}
		if (t instanceof CompoundTerm ct) {
			t = CompoundTerm.create(ct.functor(), getInstance(ct.args(), subst));
		}

		return FList.cons(t, getInstance(rest, subst));
	}

	static Term getInstance(Variable var, Map<Variable, ? extends Term> subst) {
		assert var != null && subst != null;

		var res = subst.get(var);
		return (res != null) ? res : var;
	}

	static Term getInstance(Variable var, Pair<Variable, ? extends Term> subst) {
		assert var != null && subst != null;

		return subst.get1st().equals(var) ? subst.get2nd() : var;
	}
}
