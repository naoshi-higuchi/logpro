package jp.nhiguchi.libs.logpro.program.term;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

/**
 * An immutable compound term consisting of a {@link Functor} applied to a fixed
 * list of argument {@link Term}s.
 *
 * <p>A compound term represents structured data such as {@code parent(ann, bob)} or
 * {@code point(X, Y)}. The functor's arity must equal the number of arguments.
 */
public final class CompoundTerm implements Term {
	private final Functor fFunctor;
	private final FList<? extends Term> fArgs;

	private CompoundTerm(Functor functor, FList<? extends Term> args) {
		if (functor == null || args == null) throw new NullPointerException();
		if (functor.arity() != args.size()) throw new IllegalArgumentException();

		fFunctor = functor;
		fArgs = args;
	}

	/**
	 * Creates a {@code CompoundTerm} from a functor and a persistent functional list of arguments.
	 *
	 * @param functor the functor; must not be {@code null}
	 * @param args    the argument list; size must equal {@code functor.arity()}
	 * @return a new {@code CompoundTerm}
	 * @throws NullPointerException     if {@code functor} or {@code args} is {@code null}
	 * @throws IllegalArgumentException if {@code args.size() != functor.arity()}
	 */
	public static CompoundTerm create(Functor functor, FList<? extends Term> args) {
		return new CompoundTerm(functor, args);
	}

	/**
	 * Creates a {@code CompoundTerm} from a functor and a standard {@link List} of arguments.
	 *
	 * @param functor the functor; must not be {@code null}
	 * @param args    the argument list; size must equal {@code functor.arity()}
	 * @return a new {@code CompoundTerm}
	 * @throws NullPointerException     if {@code functor} or {@code args} is {@code null}
	 * @throws IllegalArgumentException if {@code args.size() != functor.arity()}
	 */
	public static CompoundTerm create(Functor functor, List<? extends Term> args) {
		return new CompoundTerm(functor, FList.flist(args));
	}

	/**
	 * Creates a {@code CompoundTerm} from a functor and a varargs array of arguments.
	 *
	 * @param functor the functor; must not be {@code null}
	 * @param args    the arguments; count must equal {@code functor.arity()}
	 * @return a new {@code CompoundTerm}
	 * @throws NullPointerException     if {@code functor} is {@code null}
	 * @throws IllegalArgumentException if the argument count does not match {@code functor.arity()}
	 */
	public static CompoundTerm create(Functor functor, Term... args) {
		return new CompoundTerm(functor, FList.flist(args));
	}

	/**
	 * Returns the functor of this compound term.
	 *
	 * @return the functor
	 */
	public Functor functor() {
		return fFunctor;
	}

	/**
	 * Returns the argument list of this compound term.
	 *
	 * @return an immutable list of argument terms
	 */
	public FList<? extends Term> args() {
		return fArgs;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof CompoundTerm rhs)) return false;

		return Objects.equals(fFunctor, rhs.fFunctor)
				&& Objects.equals(fArgs, rhs.fArgs);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fFunctor) + Objects.hashCode(fArgs);
	}

	@Override
	public String toString() {
		return "%s(%s)".formatted(fFunctor, fArgs.toStringWithoutBrackets());
	}
}
