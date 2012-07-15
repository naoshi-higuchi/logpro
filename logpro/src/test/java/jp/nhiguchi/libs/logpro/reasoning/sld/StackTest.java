/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.reasoning.sld;

import java.util.*;

import jp.nhiguchi.libs.tuple.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Naoshi Higuchi
 */
public class StackTest {
	public StackTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of emptyStack method, of class Stack.
	 */
	@Test
	public void testEmptyStack() {
		System.out.println("emptyStack");
		Stack result = Stack.emptyStack();
		Iterator<Resolution> it = result.descendingIterator();
		assertFalse(it.hasNext());
	}

	/**
	 * Test of push method, of class Stack.
	 */
	@Test
	public void testPush() {
		System.out.println("push");
		Goal goal = Goal.newGoal(query("?- p(X), q(X)."), null);
		Clause inputClause = clause("p(a) :- r(a).");
		Resolution resolution = Resolution.resolve(goal, inputClause, null).get2nd();
		Stack instance = Stack.emptyStack();
		Stack result = instance.push(resolution);
		Iterator<Resolution> it = result.descendingIterator();
		assertTrue(it.hasNext());
		assertEquals(resolution, it.next());
		assertFalse(it.hasNext());
	}

	/**
	 * Test of peek method, of class Stack.
	 */
	@Test
	public void testPeek() {
		System.out.println("peek");
		Stack instance;
		Resolution expResult;
		Resolution result;

		instance = Stack.emptyStack();
		expResult = null;
		result = instance.peek();
		assertEquals(expResult, result);

		Goal goal = Goal.newGoal(query("?- p(X), q(X)."), null);
		Resolution r = Resolution.resolve(
				goal, clause("p(a) :- r(a)."), null).get2nd();
		instance = instance.push(r);
		expResult = r;
		result = instance.peek();
		assertEquals(expResult, result);

	}

	/**
	 * Test of iterator method, of class Stack.
	 */
	@Test
	public void testIterator() {
		System.out.println("iterator");
		Stack instance;
		Iterator<Resolution> result;

		Pair<Goal, Resolution> p0 = Resolution.resolve(
				Goal.newGoal(query("?- p(X), q(X)."), null),
				clause("p(a) :- r(a)."),
				null);
		Goal goal = p0.get1st();
		Resolution r0 = p0.get2nd();

		Pair<Goal, Resolution> p1 = Resolution.resolve(
				goal, clause("r(Y) :- s(c, Y)."), null);
		Resolution r1 = p1.get2nd();

		instance = Stack.emptyStack();
		instance = instance.push(r0);
		instance = instance.push(r1);

		result = instance.iterator();
		assertTrue(result.hasNext());
		assertEquals(r0, result.next());
		assertTrue(result.hasNext());
		assertEquals(r1, result.next());
		assertFalse(result.hasNext());
	}

	/**
	 * Test of descendingIterator method, of class Stack.
	 */
	@Test
	public void testDescendingIterator() {
		System.out.println("descendingIterator");
		Stack instance;
		Iterator<Resolution> result;

		Pair<Goal, Resolution> p0 = Resolution.resolve(
				Goal.newGoal(query("?- p(X), q(X)."), null),
				clause("p(a) :- r(a)."),
				null);
		Goal goal = p0.get1st();
		Resolution r0 = p0.get2nd();

		Pair<Goal, Resolution> p1 = Resolution.resolve(
				goal, clause("r(Y) :- s(c, Y)."), null);
		Resolution r1 = p1.get2nd();

		instance = Stack.emptyStack();
		instance = instance.push(r0);
		instance = instance.push(r1);

		result = instance.descendingIterator();
		assertTrue(result.hasNext());
		assertEquals(r1, result.next());
		assertTrue(result.hasNext());
		assertEquals(r0, result.next());
		assertFalse(result.hasNext());
	}

	/**
	 * Test of isLoop method, of class Stack.
	 */
	@Test
	public void testIsLoop() {
		System.out.println("isLoop");
		Stack instance;
		boolean expResult;
		boolean result;

		Goal goal = Goal.newGoal(query("?- path(a, X)."), null);
		Clause inCls0 = clause("path(X, Y) :- arc(X, Z), path(Z, Y).");
		Clause inCls1 = fact("arc(a, b).");
		Clause inCls2 = inCls0;
		Clause inCls3 = fact("arc(b, a).");

		instance = Stack.emptyStack();

		Pair<Goal, Resolution> p0 = Resolution.resolve(
				goal, inCls0, null);
		instance = instance.push(p0.get2nd());

		expResult = false;
		result = instance.isLoop(p0.get1st());
		assertEquals(expResult, result);

		Pair<Goal, Resolution> p1 = Resolution.resolve(
				p0.get1st(), inCls1, null);
		instance = instance.push(p1.get2nd());

		expResult = false;
		result = instance.isLoop(p1.get1st());
		assertEquals(expResult, result);

		Pair<Goal, Resolution> p2 = Resolution.resolve(p1.get1st(), inCls2, null);
		instance = instance.push(p2.get2nd());

		expResult = false;
		result = instance.isLoop(p2.get1st());
		assertEquals(expResult, result);

		Pair<Goal, Resolution> p3 = Resolution.resolve(p2.get1st(), inCls3, null);
		instance = instance.push(p3.get2nd());

		expResult = true;
		result = instance.isLoop(p3.get1st());
		assertEquals(expResult, result);
	}
}
