/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.formula.*;

/**
 *
 * @author Naoshi Higuchi
 */
public interface Program {
	/**
	 *
	 * @param literal
	 * @return a List of Clause. If no clause is found, empty list.
	 */
	public List<Clause> clauses(AtomicFormula literal);
}
