/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

/**
 * Immutable.
 *
 * @author Naoshi Higuchi
 */
final class Stack implements Iterable<Resolution> {
	private static class StackElem {
		private final Resolution fResolution;
		private final int fLoopHash;

		private StackElem(Resolution resolution) {
			fResolution = resolution;
			fLoopHash = getLoopHash(resolution.getGoal());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof StackElem)) return false;

			StackElem rhs = (StackElem) obj;

			return Objects.equals(fResolution, rhs.fResolution)
					&& Objects.equals(fLoopHash, rhs.fLoopHash);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(fResolution)
					+ Objects.hashCode(fLoopHash);
		}

		@Override
		public String toString() {
			return String.format("[resolution=%s, loopHash=%d", fResolution, fLoopHash);
		}
	}

	private static class IteratorImpl implements Iterator<Resolution> {
		private final Iterator<StackElem> fIt;

		private IteratorImpl(Iterator<StackElem> it) {
			fIt = it;
		}

		public boolean hasNext() {
			return fIt.hasNext();
		}

		public Resolution next() {
			if (!fIt.hasNext()) throw new NoSuchElementException();

			StackElem se = fIt.next();

			return se.fResolution;
		}

		public void remove() {
			throw new UnsupportedOperationException("Immutable.");
		}
	}
	private final FList<StackElem> fElems;

	private Stack(FList<StackElem> elems) {
		fElems = elems;
	}

	public static Stack emptyStack() {
		FList<StackElem> empty = FList.flist();
		return new Stack(empty);
	}

	private static int getLoopHash(Goal goal) {
		FList<AtomicFormula> subGoals = goal.toSubGoals();
		if (subGoals.isEmpty()) return 0;

		return subGoals.head().predicate().hashCode();
	}

	public Stack push(Resolution resolution) {
		return new Stack(FList.cons(new StackElem(resolution), fElems));
	}

	public Resolution peek() {
		if (fElems.isEmpty()) return null;

		return fElems.head().fResolution;
	}

	/**
	 *
	 * @return An iterator which iterates from the bottom to the top.
	 */
	@Override
	public Iterator<Resolution> iterator() {
		return new IteratorImpl(fElems.reverse().iterator());
	}

	/**
	 *
	 * @return An iterator which iterates from the top to the bottom.
	 */
	public Iterator<Resolution> descendingIterator() {
		return new IteratorImpl(fElems.iterator());
	}

	public boolean isLoop(Goal goal) {
		if (fElems.isEmpty()) return false;
		if (goal.toSubGoals().isEmpty()) return false;

		int hash = getLoopHash(goal);
		FList<AtomicFormula> subGoals = goal.toSubGoals();

		for (StackElem se : fElems) {
			if (hash == se.fLoopHash) {
				FList<AtomicFormula> seSubGoals = se.fResolution.getGoal().toSubGoals();

				if (subGoals.size() < seSubGoals.size()) continue;

				AtomicFormula subGoal = subGoals.head();
				AtomicFormula seSubGoal = seSubGoals.head();

				if (Util.isAlphaEquivalent(subGoal, seSubGoal)) return true;
			}
		}

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Stack)) return false;

		Stack rhs = (Stack) obj;

		return Objects.equals(fElems, rhs.fElems);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fElems);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<StackElem> it = fElems.iterator();
		while (it.hasNext()) {
			if (sb.length() == 0) {
				sb.append("Stack(");
			} else {
				sb.append('\n');
			}
			sb.append(it.next().toString());
		}
		sb.append(")");
		return sb.toString();
	}
}
