package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

final class Unification {
	static Map<Variable, ? extends Term> getMGU(AtomicFormula subGoal, AtomicFormula inputHead) {
		if (!subGoal.predicate().equals(inputHead.predicate())) return null;

		Map<Variable, Term> mgu = new HashMap<>();

		var ds = getDisagreementSet(subGoal, inputHead);
		while (ds != null) {
			var subst = getSubstitute(ds.get1st(), ds.get2nd());
			if (subst == null) return null;

			subGoal = Instances.getInstance(subGoal, subst);
			inputHead = Instances.getInstance(inputHead, subst);
			mgu = composeSubstitute(mgu, subst);

			ds = getDisagreementSet(subGoal, inputHead);
		}
		return mgu;
	}

	private static Map<Variable, Term> composeSubstitute(
			Map<Variable, ? extends Term> subst, Pair<Variable, ? extends Term> s) {
		var res = new HashMap<Variable, Term>();

		for (var ent : subst.entrySet()) {
			Term t = ent.getValue();
			if (t instanceof Variable v) {
				t = Instances.getInstance(v, s);
			}
			res.put(ent.getKey(), t);
		}

		if (!res.containsKey(s.get1st())) {
			res.put(s.get1st(), s.get2nd());
		}

		return res;
	}

	private static Pair<? extends Term, ? extends Term> getDisagreementSet(
			AtomicFormula subGoal, AtomicFormula inputHead) {
		assert subGoal.predicate().equals(inputHead.predicate());

		var sgArgs = subGoal.args();
		var ihArgs = inputHead.args();

		while (!sgArgs.isEmpty()) {
			var sgArg = sgArgs.head();
			sgArgs = sgArgs.tail();

			var ihArg = ihArgs.head();
			ihArgs = ihArgs.tail();

			if (sgArg.equals(ihArg)) continue;

			return Pair.newPair(sgArg, ihArg);
		}

		return null;
	}

	private static Pair<Variable, ? extends Term> getSubstitute(Term term1, Term term2) {
		assert !term1.equals(term2);

		if (term1 instanceof Variable v) {
			return getSubstitute(v, term2);
		}

		if (term2 instanceof Variable v) {
			return getSubstitute(v, term1);
		}

		if (term1 instanceof CompoundTerm ct1 && term2 instanceof CompoundTerm ct2) {
			if (!ct1.functor().equals(ct2.functor())) return null;

			var args1 = ct1.args();
			var args2 = ct2.args();

			while (!args1.isEmpty()) {
				assert !args2.isEmpty();

				var a1 = args1.head();
				var a2 = args2.head();

				if (!a1.equals(a2)) return getSubstitute(a1, a2);

				args1 = args1.tail();
				args2 = args2.tail();
			}
			assert false; // never reached: term1 != term2 but all args equal
		}

		return null;
	}

	private static Pair<Variable, ? extends Term> getSubstitute(Variable v, Term t) {
		return occurs(v, t) ? null : Pair.newPair(v, t);
	}

	private static boolean occurs(Variable v, Term t) {
		if (v.equals(t)) return true;

		if (t instanceof CompoundTerm ct) {
			for (var arg : ct.args()) {
				if (occurs(v, arg)) return true;
			}
		}

		return false;
	}
}
