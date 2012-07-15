/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.term;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author naoshi
 */
public class AtomTest {
	public AtomTest() {
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
	 * Test of create method, of class Atom.
	 */
	@Test
	public void testCreate() {
		System.out.println("create");
		Object value;
		Atom result;

		value = "string";
		result = Atom.create(value);
		assertEquals(value, result.value());

		value = new Integer(123);
		result = Atom.create(value);
		assertEquals(value, result.value());
	}
}
