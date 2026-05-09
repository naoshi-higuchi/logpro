package jp.nhiguchi.libs.logpro.program.term;

/**
 * Base sealed interface for all logical terms.
 *
 * <p>A term is one of:
 * <ul>
 *   <li>{@link Variable} — a logical variable (e.g., {@code X}, {@code Y})</li>
 *   <li>{@link Atom} — an atomic constant value (e.g., {@code ann}, {@code bob})</li>
 *   <li>{@link CompoundTerm} — a functor applied to argument terms (e.g., {@code parent(ann, bob)})</li>
 * </ul>
 *
 * <p>The sealed hierarchy enables exhaustive pattern matching over all term kinds.
 */
public sealed interface Term permits Variable, Constant, CompoundTerm {
}
