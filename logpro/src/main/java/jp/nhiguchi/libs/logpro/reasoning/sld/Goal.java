/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import static jp.nhiguchi.libs.flist.FList.*;
import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

/**
 *
 * @author Naoshi Higuchi
 */
final class Goal implements Iterable<Goal.Entry> {
	static class Entry {
		private final AtomicFormula fSubGoal;
		private final SLDBranch fCutPoint;
		private final boolean fForkSuppressiveZone;

		private Entry(AtomicFormula subGoal, SLDBranch cutPoint,
				boolean forkSuppressiveZone) {
			assert (subGoal != null);
			assert (cutPoint == null ? true : subGoal.isCut());

			fSubGoal = subGoal;
			fCutPoint = cutPoint;
			fForkSuppressiveZone = forkSuppressiveZone;
		}

		AtomicFormula getSubGoal() {
			return fSubGoal;
		}

		SLDBranch getCutPoint() {
			return fCutPoint;
		}

		boolean isForkSuppressiveZone() {
			return fForkSuppressiveZone;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof Entry)) return false;

			Entry rhs = (Entry) obj;

			return Objects.equals(fSubGoal, rhs.fSubGoal)
					&& Objects.equals(fCutPoint, rhs.fCutPoint)
					&& Objects.equals(fForkSuppressiveZone, rhs.fForkSuppressiveZone);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(fSubGoal)
					+ Objects.hashCode(fCutPoint)
					+ Objects.hashCode(fForkSuppressiveZone);
		}

		@Override
		public String toString() {
			return String.format("<%s, %s, %s>",
					fSubGoal, fCutPoint, fForkSuppressiveZone);
		}
	}
	private final FList<Entry> fEntries;

	private Goal(FList<Entry> elems) {
		fEntries = elems;
	}

	static Goal newInitialGoal(Clause initialGoal) {
		if (!initialGoal.isQuery()) throw new IllegalArgumentException();

		return newGoal(initialGoal, SLDBranch.NULL);
	}

	static Goal newGoal(Clause goal, SLDBranch cutPoint) {
		FList<Entry> elems = flist();
		FList<AtomicFormula> rest = goal.body();
		while (!rest.isEmpty()) {
			boolean forkSuppressiveZone = rest.contains(AtomicFormula.CUT);

			AtomicFormula subGoal = rest.head();
			rest = rest.tail();

			SLDBranch cp = null;
			if (subGoal.isCut()) cp = cutPoint;

			Entry elem = new Entry(subGoal, cp, forkSuppressiveZone);
			elems = cons(elem, elems);
		}

		return new Goal(elems.reverse());
	}

	public Iterator<Goal.Entry> iterator() {
		return fEntries.iterator();
	}

	Goal nextGoal(
			Clause unified, Map<Variable, ? extends Term> mgu,
			SLDBranch cutPoint) {
		FList<Goal.Entry> entries = flist();

		Goal newGoal = Goal.newGoal(unified, cutPoint);
		for (Goal.Entry ent : newGoal) {
			entries = cons(ent, entries);
		}

		Goal restGoal = pop().get2nd();
		for (Goal.Entry ent : restGoal) {
			AtomicFormula sg = Instances.getInstance(ent.getSubGoal(), mgu);
			entries = cons(
					new Goal.Entry(
					sg, ent.getCutPoint(),
					ent.isForkSuppressiveZone()), entries);
		}

		return new Goal(entries.reverse());
	}

	Pair<Entry, Goal> pop() {
		Entry popped = fEntries.head();
		FList<Entry> rest = fEntries.tail();

		return Pair.newPair(popped, new Goal(rest));
	}

	Entry peek() {
		if (fEntries.isEmpty()) return null;

		return fEntries.head();
	}

	boolean isEmpty() {
		return fEntries.isEmpty();
	}

	Clause toClause() {
		FList<AtomicFormula> body = toSubGoals();
		return Clause.query(body);
	}

	FList<AtomicFormula> toSubGoals() {
		FList<AtomicFormula> subGoals = flist();
		for (Entry elem : fEntries) {
			subGoals = cons(elem.getSubGoal(), subGoals);
		}

		return subGoals.reverse();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Goal)) return false;

		Goal rhs = (Goal) obj;

		return Objects.equals(fEntries, rhs.fEntries);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fEntries);
	}

	@Override
	public String toString() {
		return fEntries.toString();
	}
}
