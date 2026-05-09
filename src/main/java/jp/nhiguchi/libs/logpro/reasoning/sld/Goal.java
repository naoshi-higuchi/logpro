package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import static jp.nhiguchi.libs.flist.FList.*;
import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;

final class Goal implements Iterable<Goal.Entry> {
	record Entry(AtomicFormula subGoal, SLDBranch cutPoint, boolean forkSuppressiveZone) {
		Entry {
			assert subGoal != null;
			assert cutPoint == null || subGoal.isCut();
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
		var elems = FList.<Entry>flist();
		var rest = goal.body();
		while (!rest.isEmpty()) {
			boolean forkSuppressiveZone = rest.contains(AtomicFormula.CUT);
			var subGoal = rest.head();
			rest = rest.tail();

			SLDBranch cp = subGoal.isCut() ? cutPoint : null;
			elems = cons(new Entry(subGoal, cp, forkSuppressiveZone), elems);
		}
		return new Goal(elems.reverse());
	}

	@Override
	public Iterator<Goal.Entry> iterator() {
		return fEntries.iterator();
	}

	Goal nextGoal(Clause unified, Map<Variable, ? extends Term> mgu, SLDBranch cutPoint) {
		var entries = FList.<Entry>flist();

		for (var ent : Goal.newGoal(unified, cutPoint)) {
			entries = cons(ent, entries);
		}

		for (var ent : pop().get2nd()) {
			var sg = Instances.getInstance(ent.subGoal(), mgu);
			entries = cons(new Entry(sg, ent.cutPoint(), ent.forkSuppressiveZone()), entries);
		}

		return new Goal(entries.reverse());
	}

	Pair<Entry, Goal> pop() {
		return Pair.newPair(fEntries.head(), new Goal(fEntries.tail()));
	}

	Entry peek() {
		return fEntries.isEmpty() ? null : fEntries.head();
	}

	boolean isEmpty() {
		return fEntries.isEmpty();
	}

	Clause toClause() {
		return Clause.query(toSubGoals());
	}

	FList<AtomicFormula> toSubGoals() {
		var subGoals = FList.<AtomicFormula>flist();
		for (var elem : fEntries) {
			subGoals = cons(elem.subGoal(), subGoals);
		}
		return subGoals.reverse();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Goal rhs)) return false;
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
