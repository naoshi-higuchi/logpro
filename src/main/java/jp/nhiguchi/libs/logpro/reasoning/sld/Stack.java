package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.flist.*;
import jp.nhiguchi.libs.logpro.program.formula.*;

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
			if (obj == this) return true;
			if (!(obj instanceof StackElem rhs)) return false;
			return Objects.equals(fResolution, rhs.fResolution)
					&& fLoopHash == rhs.fLoopHash;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(fResolution) + Integer.hashCode(fLoopHash);
		}

		@Override
		public String toString() {
			return "[resolution=%s, loopHash=%d".formatted(fResolution, fLoopHash);
		}
	}

	private final FList<StackElem> fElems;

	private Stack(FList<StackElem> elems) {
		fElems = elems;
	}

	public static Stack emptyStack() {
		return new Stack(FList.flist());
	}

	private static int getLoopHash(Goal goal) {
		var subGoals = goal.toSubGoals();
		return subGoals.isEmpty() ? 0 : subGoals.head().predicate().hashCode();
	}

	public Stack push(Resolution resolution) {
		return new Stack(FList.cons(new StackElem(resolution), fElems));
	}

	public Resolution peek() {
		return fElems.isEmpty() ? null : fElems.head().fResolution;
	}

	/** Iterates from the bottom to the top. */
	@Override
	public Iterator<Resolution> iterator() {
		return new MappedIterator(fElems.reverse().iterator());
	}

	/** Iterates from the top to the bottom. */
	public Iterator<Resolution> descendingIterator() {
		return new MappedIterator(fElems.iterator());
	}

	public boolean isLoop(Goal goal) {
		if (fElems.isEmpty() || goal.toSubGoals().isEmpty()) return false;

		int hash = getLoopHash(goal);
		var subGoals = goal.toSubGoals();

		for (var se : fElems) {
			if (hash == se.fLoopHash) {
				var seSubGoals = se.fResolution.getGoal().toSubGoals();
				if (subGoals.size() < seSubGoals.size()) continue;
				if (Util.isAlphaEquivalent(subGoals.head(), seSubGoals.head())) return true;
			}
		}

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Stack rhs)) return false;
		return Objects.equals(fElems, rhs.fElems);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fElems);
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		for (var se : fElems) {
			if (!sb.isEmpty()) sb.append('\n');
			sb.append(se);
		}
		return "Stack(%s)".formatted(sb);
	}

	private static final class MappedIterator implements Iterator<Resolution> {
		private final Iterator<StackElem> fIt;

		private MappedIterator(Iterator<StackElem> it) {
			fIt = it;
		}

		@Override
		public boolean hasNext() {
			return fIt.hasNext();
		}

		@Override
		public Resolution next() {
			if (!fIt.hasNext()) throw new NoSuchElementException();
			return fIt.next().fResolution;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Immutable.");
		}
	}
}
