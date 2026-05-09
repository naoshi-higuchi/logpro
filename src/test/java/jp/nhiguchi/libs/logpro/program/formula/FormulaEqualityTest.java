package jp.nhiguchi.libs.logpro.program.formula;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.term.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FormulaEqualityTest {

	@Test
	public void testPredicateEquality() {
		assertEquals(Predicate.create("parent", 2), Predicate.create("parent", 2));
		assertNotEquals(Predicate.create("parent", 2), Predicate.create("parent", 3));
		assertNotEquals(Predicate.create("parent", 2), Predicate.create("child", 2));
	}

	@Test
	public void testPredicateHashCode() {
		assertEquals(
				Predicate.create("parent", 2).hashCode(),
				Predicate.create("parent", 2).hashCode());
	}

	@Test
	public void testPredicateInvalidArgs() {
		assertThrows(IllegalArgumentException.class, () -> Predicate.create(null, 1));
		assertThrows(IllegalArgumentException.class, () -> Predicate.create("p", -1));
	}

	@Test
	public void testAtomicFormulaEquality() {
		var p = Predicate.create("parent", 2);
		var f1 = AtomicFormula.create(p, Atom.create("ann"), Atom.create("bob"));
		var f2 = AtomicFormula.create(p, Atom.create("ann"), Atom.create("bob"));
		var f3 = AtomicFormula.create(p, Atom.create("ann"), Atom.create("cate"));
		assertEquals(f1, f2);
		assertNotEquals(f1, f3);
	}

	@Test
	public void testAtomicFormulaHashCode() {
		var p = Predicate.create("parent", 2);
		var f1 = AtomicFormula.create(p, Atom.create("ann"), Atom.create("bob"));
		var f2 = AtomicFormula.create(p, Atom.create("ann"), Atom.create("bob"));
		assertEquals(f1.hashCode(), f2.hashCode());
	}

	@Test
	public void testAtomicFormulaUsableAsMapKey() {
		var p = Predicate.create("p", 1);
		var f1 = AtomicFormula.create(p, Atom.create("a"));
		var f2 = AtomicFormula.create(p, Atom.create("a"));
		var map = new HashMap<AtomicFormula, String>();
		map.put(f1, "value");
		assertEquals("value", map.get(f2));
	}
}
