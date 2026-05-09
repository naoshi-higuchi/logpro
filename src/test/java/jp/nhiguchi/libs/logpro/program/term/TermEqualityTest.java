package jp.nhiguchi.libs.logpro.program.term;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TermEqualityTest {

	@Test
	public void testAtomEquality() {
		assertEquals(Atom.create("ann"), Atom.create("ann"));
		assertNotEquals(Atom.create("ann"), Atom.create("bob"));
		assertEquals(Atom.create(42), Atom.create(42));
		assertNotEquals(Atom.create("ann"), Atom.create(42));
	}

	@Test
	public void testAtomHashCode() {
		assertEquals(Atom.create("ann").hashCode(), Atom.create("ann").hashCode());
		assertEquals(Atom.create(42).hashCode(), Atom.create(42).hashCode());
	}

	@Test
	public void testAtomNullReject() {
		assertThrows(IllegalArgumentException.class, () -> Atom.create(null));
	}

	@Test
	public void testVariableEquality() {
		assertEquals(Variable.create("X"), Variable.create("X"));
		assertNotEquals(Variable.create("X"), Variable.create("Y"));
	}

	@Test
	public void testVariableHashCode() {
		assertEquals(Variable.create("X").hashCode(), Variable.create("X").hashCode());
	}

	@Test
	public void testVariableNullReject() {
		assertThrows(IllegalArgumentException.class, () -> Variable.create(null));
	}

	@Test
	public void testFunctorEquality() {
		assertEquals(Functor.create("f", 2), Functor.create("f", 2));
		assertNotEquals(Functor.create("f", 2), Functor.create("f", 3));
		assertNotEquals(Functor.create("f", 2), Functor.create("g", 2));
	}

	@Test
	public void testFunctorHashCode() {
		assertEquals(Functor.create("f", 2).hashCode(), Functor.create("f", 2).hashCode());
	}

	@Test
	public void testCompoundTermEquality() {
		var f = Functor.create("f", 2);
		var t1 = CompoundTerm.create(f, Atom.create("a"), Atom.create("b"));
		var t2 = CompoundTerm.create(f, Atom.create("a"), Atom.create("b"));
		var t3 = CompoundTerm.create(f, Atom.create("a"), Atom.create("c"));
		assertEquals(t1, t2);
		assertNotEquals(t1, t3);
	}

	@Test
	public void testCompoundTermHashCode() {
		var f = Functor.create("f", 2);
		var t1 = CompoundTerm.create(f, Atom.create("a"), Atom.create("b"));
		var t2 = CompoundTerm.create(f, Atom.create("a"), Atom.create("b"));
		assertEquals(t1.hashCode(), t2.hashCode());
	}

	@Test
	public void testCompoundTermArityMismatch() {
		var f = Functor.create("f", 2);
		assertThrows(IllegalArgumentException.class,
				() -> CompoundTerm.create(f, Atom.create("a")));
	}
}
