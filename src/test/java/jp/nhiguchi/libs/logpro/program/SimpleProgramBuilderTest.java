package jp.nhiguchi.libs.logpro.program;

import java.util.*;

import jp.nhiguchi.libs.logpro.program.formula.*;
import jp.nhiguchi.libs.logpro.program.term.*;
import jp.nhiguchi.libs.logpro.reasoning.sld.SLD;
import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleProgramBuilderTest {

    @Test
    public void testAddAll() {
        var clauses = List.of(
                fact("parent(ann, cate)."),
                fact("parent(bob, cate)."),
                fact("female(ann)."),
                clause("mother(X, Y) :- parent(X, Y), female(X)."));
        Program program = new SimpleProgramBuilder().addAll(clauses).toProgram();

        Map<Variable, Term> answer = SLD.solveOne(program, query("?- mother(X, cate)."));
        assertNotNull(answer);
        assertEquals(term("ann"), answer.get(variable("X")));
    }

    @Test
    public void testAddAll_deduplication() {
        Clause c = fact("parent(ann, cate).");
        Program program = new SimpleProgramBuilder().addAll(List.of(c, c)).toProgram();

        Set<Map<Variable, Term>> answers = SLD.solveAll(program, query("?- parent(ann, X)."));
        assertEquals(1, answers.size());
    }

    @Test
    public void testAddAll_queryThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new SimpleProgramBuilder().addAll(List.of(query("?- parent(ann, cate)."))));
    }
}
