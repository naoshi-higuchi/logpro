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
final class Unification {
	/**
	 * Get a MGU (Most Generic Unifier).
	 *
	 * @param subGoal
	 * @param inputHead
	 * @return The mgu. If unification failed, returns null.
	 */
	static Map<Variable, ? extends Term> getMGU(AtomicFormula subGoal, AtomicFormula inputHead) {
		if (!subGoal.predicate().equals(inputHead.predicate())) return null;

		Map<Variable, ? extends Term> mgu = new HashMap<Variable, Term>();

		Pair<? extends Term, ? extends Term> ds = getDisagreementSet(subGoal, inputHead);
		while (ds != null) {
			Pair<Variable, ? extends Term> subst = getSubstitute(ds.get1st(), ds.get2nd());
			if (subst == null) return null;

			subGoal = Instances.getInstance(subGoal, subst);
			inputHead = Instances.getInstance(inputHead, subst);

			mgu = composeSubstitute(mgu, subst);

			ds = getDisagreementSet(subGoal, inputHead);
		}
		return mgu;
	}

	private static Map<Variable, ? extends Term> composeSubstitute(Map<Variable, ? extends Term> subst, Pair<Variable, ? extends Term> s) {
		Map<Variable, Term> res = new HashMap<Variable, Term>();

		for (Map.Entry<Variable, ? extends Term> ent : subst.entrySet()) {
			Term t = ent.getValue();
			if (t instanceof Variable) {
				t = Instances.getInstance((Variable) t, s);
			}
			res.put(ent.getKey(), t);
		}

		if (!res.containsKey(s.get1st())) {
			res.put(s.get1st(), s.get2nd());
		}

		return res;
	}

	private static Pair<? extends Term, ? extends Term> getDisagreementSet(AtomicFormula subGoal, AtomicFormula inputHead) {
		assert (subGoal.predicate().equals(inputHead.predicate()));

		FList<? extends Term> sgArgs = subGoal.args();
		FList<? extends Term> ihArgs = inputHead.args();

		while (!sgArgs.isEmpty()) {
			Term sgArg = sgArgs.head();
			sgArgs = sgArgs.tail();

			Term ihArg = ihArgs.head();
			ihArgs = ihArgs.tail();

			if (sgArg.equals(ihArg)) continue;

			return Pair.newPair(sgArg, ihArg);
		}

		return null;
	}

	private static Pair<Variable, ? extends Term> getSubstitute(Term term1, Term term2) {
		assert (!term1.equals(term2));

		if (term1 instanceof Variable) {
			Variable v = (Variable) term1;
			return getSubstitute(v, term2);
		}

		if (term2 instanceof Variable) {
			Variable v = (Variable) term2;
			return getSubstitute(v, term1);
		}

		if (term1 instanceof CompoundTerm && term2 instanceof CompoundTerm) {
			CompoundTerm ct1 = (CompoundTerm) term1;
			CompoundTerm ct2 = (CompoundTerm) term2;

			if (!ct1.functor().equals(ct2.functor())) return null;

			FList<? extends Term> args1 = ct1.args();
			FList<? extends Term> args2 = ct2.args();

			while (!args1.isEmpty()) {
				assert (!args2.isEmpty());

				Term a1 = args1.head();
				Term a2 = args2.head();

				if (a1.equals(a2)) {
					args1 = args1.tail();
					args2 = args2.tail();
					continue;
				}

				return getSubstitute(a1, a2);
			}
			assert (false); // This line is never executed because term1 does not equal to term2.
		}

		return null;
	}

	private static Pair<Variable, ? extends Term> getSubstitute(Variable v, Term t) {
		if (!occurs(v, t)) {
			return Pair.newPair(v, t);
		}
		return null;
	}

	private static boolean occurs(Variable v, Term t) {
		if (v.equals(t)) return true;

		if (t instanceof CompoundTerm) {
			CompoundTerm ct = (CompoundTerm) t;

			for (Term arg : ct.args()) {
				if (occurs(v, arg)) return true;
			}
			return false;
		}

		return false;
	}
}
