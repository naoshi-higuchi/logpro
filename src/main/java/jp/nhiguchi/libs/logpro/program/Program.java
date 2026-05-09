package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.formula.*;

public interface Program {
	List<Clause> clauses(AtomicFormula literal);
}
