/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.term;

import java.util.*;

import jp.nhiguchi.libs.flist.*;

/**
 *
 * @author Naoshi Higuchi
 */
public final class CompoundTerm implements Term {
	private final Functor fFunctor;
	private final FList<? extends Term> fArgs;

	private CompoundTerm(Functor functor, FList<? extends Term> args) {
		if (functor == null || args == null) throw new NullPointerException();

		if (functor.arity() != args.size())
			throw new IllegalArgumentException();

		fFunctor = functor;
		fArgs = args;
	}

	public static CompoundTerm create(Functor functor, FList<? extends Term> args) {
		return new CompoundTerm(functor, args);
	}

	public static CompoundTerm create(Functor functor, List<? extends Term> args) {
		FList<? extends Term> fargs = FList.flist(args);
		return new CompoundTerm(functor, fargs);
	}

	public static CompoundTerm create(Functor functor, Term... args) {
		return new CompoundTerm(functor, FList.flist(args));
	}

	public Functor functor() {
		return fFunctor;
	}

	public FList<? extends Term> args() {
		return fArgs;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof CompoundTerm)) return false;

		CompoundTerm rhs = (CompoundTerm) obj;

		return Objects.equals(fFunctor, rhs.fFunctor)
				&& Objects.equals(fArgs, rhs.fArgs);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(fFunctor)
				+ Objects.hashCode(fArgs);
	}

	@Override
	public String toString() {
		return String.format("%s(%s)",
				fFunctor, fArgs.toStringWithoutBrackets());
	}
}
