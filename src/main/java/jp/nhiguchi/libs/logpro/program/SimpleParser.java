/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.nhiguchi.libs.logpro.program;

import java.util.List;
import jp.nhiguchi.libs.logpro.program.formula.AtomicFormula;
import jp.nhiguchi.libs.logpro.program.formula.Clause;
import jp.nhiguchi.libs.logpro.program.formula.Predicate;
import jp.nhiguchi.libs.logpro.program.term.*;
import jp.nhiguchi.libs.pcom.*;

import static jp.nhiguchi.libs.pcom.Parsers.*;

/**
 *
 * @author Naoshi Higuchi
 */
public final class SimpleParser {
	private static final RecursionMark<Term> fTermRecursionMark = new RecursionMark<Term>();
	private static final Parser<Term> fTermParser = termParser();

	private static Parser<String> trim(Parser<String> p) {
		Parser<String> spacing = expr("[ \t]*");
		return Parsers.trim(spacing, p);
	}

	private static Parser<Variable> variableParser() {
		Parser<String> varStrParser = trim(expr("[A-Z_] [a-zA-Z0-9_]*"));

		Map1<String, Variable> toVar = new Map1<String, Variable>() {
			public Variable map(String v) {
				return Variable.create(v);
			}
		};

		return map(toVar, varStrParser);
	}

	private static Parser<Atom> atomParser() {
		Parser<String> atomStrParser = trim(expr("[a-z] [a-zA-Z0-9_]*"));

		Map1<String, Atom> toAtom = new Map1<String, Atom>() {
			public Atom map(String v) {
				return Atom.create(v);
			}
		};

		return map(toAtom, atomStrParser);
	}

	private static Parser<String> commaParser() {
		return trim(string(","));
	}

	private static Parser<List<Term>> argsParser() {
		Parser<String> lpar = trim(string("("));
		Parser<List<Term>> argStrParser = sepBy(recur(fTermRecursionMark), commaParser());
		Parser<String> rpar = trim(string(")"));

		return body(lpar, argStrParser, rpar);
	}

	private static Parser<CompoundTerm> compoundTermParser() {
		Parser<String> ftStrParser = trim(expr("[a-z] [a-zA-Z0-9_]*"));

		Map2<String, List<Term>, CompoundTerm> toCT = new Map2<String, List<Term>, CompoundTerm>() {
			public CompoundTerm map(
					String fname, List<Term> args) {
				Functor ft = Functor.create(
						fname, args.size());
				return CompoundTerm.create(ft, args);
			}
		};

		return map(toCT, ftStrParser, argsParser());
	}

	private static Parser<Term> termParser() {
		return mark(fTermRecursionMark,
				or(compoundTermParser(), atomParser(), variableParser()));
	}

	private static Parser<AtomicFormula> cutParser() {
		Parser<String> cutStrParser = trim(string("!"));

		Map1<String, AtomicFormula> toCut = new Map1<String, AtomicFormula>() {
			public AtomicFormula map(String v) {
				return AtomicFormula.CUT;
			}
		};

		return map(toCut, cutStrParser);
	}

	private static Parser<AtomicFormula> normalLiteralWithoutArgsParser() {
		Parser<String> p = trim(expr("[a-z] [a-zA-Z0-9_]*"));

		Map1<String, AtomicFormula> toLiteral = new Map1<String, AtomicFormula>() {
			public AtomicFormula map(String pname) {
				return AtomicFormula.create(
						Predicate.create(pname, 0));
			}
		};

		return map(toLiteral, p);
	}

	private static Parser<AtomicFormula> normalLiteralWithArgsParser() {
		Parser<String> predStrParser = trim(expr("[a-z] [a-zA-Z0-9_]*"));

		Map2<String, List<Term>, AtomicFormula> toLiteral = new Map2<String, List<Term>, AtomicFormula>() {
			public AtomicFormula map(
					String pname, List<Term> args) {
				return AtomicFormula.create(
						Predicate.create(
						pname, args.size()),
						args);
			}
		};

		return map(toLiteral, predStrParser, argsParser());
	}

	private static Parser<AtomicFormula> normalLiteralParser() {
		return or(
				normalLiteralWithArgsParser(),
				normalLiteralWithoutArgsParser());
	}

	private static Parser<AtomicFormula> literalParser() {
		return or(normalLiteralParser(), cutParser());
	}

	private static Parser<String> periodParser() {
		return trim(string("."));
	}

	private static Parser<Clause> factParser() {
		Map1<AtomicFormula, Clause> toFact = new Map1<AtomicFormula, Clause>() {
			public Clause map(AtomicFormula literal) {
				return Clause.fact(literal);
			}
		};

		return map(toFact, followedBy(literalParser(), periodParser()));
	}

	private static Parser<Clause> queryParser() {
		Parser<String> q = trim(string("?-"));
		Parser<List<AtomicFormula>> lits = sepBy(literalParser(), commaParser());

		Map1<List<AtomicFormula>, Clause> toQuery = new Map1<List<AtomicFormula>, Clause>() {
			public Clause map(List<AtomicFormula> v) {
				return Clause.query(v);
			}
		};

		return map(toQuery, body(q, lits, periodParser()));
	}

	private static Parser<Clause> clauseParser() {
		Parser<String> larrow = trim(string(":-"));
		Parser<List<AtomicFormula>> lits = sepBy(literalParser(), commaParser());

		Map2<AtomicFormula, List<AtomicFormula>, Clause> toClause = new Map2<AtomicFormula, List<AtomicFormula>, Clause>() {
			public Clause map(AtomicFormula head,
					List<AtomicFormula> body) {
				return Clause.clause(head, body);
			}
		};

		return map(toClause,
				followedBy(literalParser(), larrow),
				followedBy(lits, periodParser()));
	}

	public static Variable variable(String name) {
		Result<Variable> pr = variableParser().parse(name);
		if (pr.isFail() || !pr.rest().isEnd())
			throw new IllegalArgumentException();

		return pr.value();
	}

	public static Atom atom(String name) {
		Result<Atom> pr = atomParser().parse(name);
		if (pr.isFail() || !pr.rest().isEnd())
			throw new IllegalArgumentException();

		return pr.value();
	}

	public static CompoundTerm compoundTerm(String desc) {
		Result<CompoundTerm> pr = compoundTermParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd())
			throw new IllegalArgumentException();

		return pr.value();
	}

	public static Term term(String desc) {
		Result<Term> pr = fTermParser.parse(desc);
		if (pr.isFail() || !pr.rest().isEnd())
			throw new IllegalArgumentException();

		return pr.value();
	}

	public static AtomicFormula literal(String desc) {
		Result<AtomicFormula> pr = literalParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd())
			throw new IllegalArgumentException();

		return pr.value();
	}

	public static Clause fact(String desc) {
		Result<Clause> pr = factParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd())
			throw new IllegalArgumentException();

		return pr.value();
	}

	public static Clause query(String desc) {
		Result<Clause> pr = queryParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd())
			throw new IllegalArgumentException();

		return pr.value();
	}

	public static Clause clause(String desc) {
		Result<Clause> pr = clauseParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd())
			throw new IllegalArgumentException();

		return pr.value();
	}
}
