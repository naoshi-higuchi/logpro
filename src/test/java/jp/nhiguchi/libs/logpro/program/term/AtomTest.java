/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program.term;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author naoshi
 */
public class AtomTest {
	public AtomTest() {
	}

	@BeforeAll
	public static void setUpClass() throws Exception {
	}

	@AfterAll
	public static void tearDownClass() throws Exception {
	}

	@BeforeEach
	public void setUp() {
	}

	@AfterEach
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
