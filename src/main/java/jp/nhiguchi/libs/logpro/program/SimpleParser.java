package jp.nhiguchi.libs.logpro.program;

import java.util.List;
import jp.nhiguchi.libs.logpro.program.formula.AtomicFormula;
import jp.nhiguchi.libs.logpro.program.formula.Clause;
import jp.nhiguchi.libs.logpro.program.formula.Predicate;
import jp.nhiguchi.libs.logpro.program.term.*;
import jp.nhiguchi.libs.pcom.*;

import static jp.nhiguchi.libs.pcom.Parsers.*;

public final class SimpleParser {
	private static final RecursionMark<Term> fTermRecursionMark = new RecursionMark<>();
	private static final Parser<Term> fTermParser = termParser();

	private static Parser<String> trim(Parser<String> p) {
		var spacing = expr("[ \t]*");
		return Parsers.trim(spacing, p);
	}

	private static Parser<Variable> variableParser() {
		var varStrParser = trim(expr("[A-Z_] [a-zA-Z0-9_]*"));
		return map((Map1<String, Variable>) Variable::create, varStrParser);
	}

	private static Parser<Atom> atomParser() {
		var atomStrParser = trim(expr("[a-z] [a-zA-Z0-9_]*"));
		return map((Map1<String, Atom>) Atom::create, atomStrParser);
	}

	private static Parser<String> commaParser() {
		return trim(string(","));
	}

	private static Parser<List<Term>> argsParser() {
		var lpar = trim(string("("));
		var argStrParser = sepBy(recur(fTermRecursionMark), commaParser());
		var rpar = trim(string(")"));
		return body(lpar, argStrParser, rpar);
	}

	private static Parser<CompoundTerm> compoundTermParser() {
		var ftStrParser = trim(expr("[a-z] [a-zA-Z0-9_]*"));
		Map2<String, List<Term>, CompoundTerm> toCT = (fname, args) ->
				CompoundTerm.create(Functor.create(fname, args.size()), args);
		return map(toCT, ftStrParser, argsParser());
	}

	private static Parser<Term> termParser() {
		return mark(fTermRecursionMark,
				or(compoundTermParser(), atomParser(), variableParser()));
	}

	private static Parser<AtomicFormula> cutParser() {
		var cutStrParser = trim(string("!"));
		return map((Map1<String, AtomicFormula>) _ -> AtomicFormula.CUT, cutStrParser);
	}

	private static Parser<AtomicFormula> normalLiteralWithoutArgsParser() {
		var p = trim(expr("[a-z] [a-zA-Z0-9_]*"));
		Map1<String, AtomicFormula> toLiteral = pname ->
				AtomicFormula.create(Predicate.create(pname, 0));
		return map(toLiteral, p);
	}

	private static Parser<AtomicFormula> normalLiteralWithArgsParser() {
		var predStrParser = trim(expr("[a-z] [a-zA-Z0-9_]*"));
		Map2<String, List<Term>, AtomicFormula> toLiteral = (pname, args) ->
				AtomicFormula.create(Predicate.create(pname, args.size()), args);
		return map(toLiteral, predStrParser, argsParser());
	}

	private static Parser<AtomicFormula> normalLiteralParser() {
		return or(normalLiteralWithArgsParser(), normalLiteralWithoutArgsParser());
	}

	private static Parser<AtomicFormula> literalParser() {
		return or(normalLiteralParser(), cutParser());
	}

	private static Parser<String> periodParser() {
		return trim(string("."));
	}

	private static Parser<Clause> factParser() {
		return map((Map1<AtomicFormula, Clause>) Clause::fact,
				followedBy(literalParser(), periodParser()));
	}

	private static Parser<Clause> queryParser() {
		var q = trim(string("?-"));
		var lits = sepBy(literalParser(), commaParser());
		return map((Map1<List<AtomicFormula>, Clause>) Clause::query,
				body(q, lits, periodParser()));
	}

	private static Parser<Clause> clauseParser() {
		var larrow = trim(string(":-"));
		var lits = sepBy(literalParser(), commaParser());
		Map2<AtomicFormula, List<AtomicFormula>, Clause> toClause =
				(head, body) -> Clause.clause(head, body);
		return map(toClause,
				followedBy(literalParser(), larrow),
				followedBy(lits, periodParser()));
	}

	public static Variable variable(String name) {
		var pr = variableParser().parse(name);
		if (pr.isFail() || !pr.rest().isEnd()) throw new IllegalArgumentException();
		return pr.value();
	}

	public static Atom atom(String name) {
		var pr = atomParser().parse(name);
		if (pr.isFail() || !pr.rest().isEnd()) throw new IllegalArgumentException();
		return pr.value();
	}

	public static CompoundTerm compoundTerm(String desc) {
		var pr = compoundTermParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd()) throw new IllegalArgumentException();
		return pr.value();
	}

	public static Term term(String desc) {
		var pr = fTermParser.parse(desc);
		if (pr.isFail() || !pr.rest().isEnd()) throw new IllegalArgumentException();
		return pr.value();
	}

	public static AtomicFormula literal(String desc) {
		var pr = literalParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd()) throw new IllegalArgumentException();
		return pr.value();
	}

	public static Clause fact(String desc) {
		var pr = factParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd()) throw new IllegalArgumentException();
		return pr.value();
	}

	public static Clause query(String desc) {
		var pr = queryParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd()) throw new IllegalArgumentException();
		return pr.value();
	}

	public static Clause clause(String desc) {
		var pr = clauseParser().parse(desc);
		if (pr.isFail() || !pr.rest().isEnd()) throw new IllegalArgumentException();
		return pr.value();
	}
}
