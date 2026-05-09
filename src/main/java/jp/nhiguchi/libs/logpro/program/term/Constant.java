package jp.nhiguchi.libs.logpro.program.term;

/**
 * Marker interface for constant (ground, non-variable) terms.
 *
 * <p>The only permitted implementation is {@link Atom}.
 * Constants are terms that contain no {@link Variable}s and are therefore
 * fully ground.
 */
public sealed interface Constant extends Term permits Atom {
}
