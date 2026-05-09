# logpro

A Java library implementing **SLD resolution** (Selective Linear Definite clause resolution) with unification — the core inference mechanism of Prolog-style logic programming.

Given a set of facts and rules (a *program*) and a query, the library finds all variable bindings (*computed answer substitutions*) that satisfy the query.

## Features

- Prolog-like syntax parsed from strings (facts, rules, queries, terms)
- Full unification with occurs check
- Lazy, on-demand solution enumeration
- Parallel solving via `ExecutorService`
- Extensible special predicates (e.g. built-ins like `atom/1`, `match/2`)
- Immutable data structures throughout — safe for concurrent use

## Prerequisites

logpro depends on three sibling libraries that are **not on Maven Central**. Install them locally before building:

| Library | Version | Directory |
|---------|---------|-----------|
| `jp.nhiguchi.libs:tuple` | 0.2 | `../tuple.git` |
| `jp.nhiguchi.libs:flist` | 0.3 | `../flist.git` |
| `jp.nhiguchi.libs:pcom`  | 0.2 | `../pcom.git`  |

In each sibling directory:

```bash
mvn install
```

Requires **Java 26** and **Maven 3.x**.

## Build

```bash
mvn clean package
```

The jar is written to `target/logpro-0.2.jar`.

## Running tests

```bash
mvn test
```

Run a specific test class:

```bash
mvn test -Dtest=SampleCodeTest
```

## Quick start

```java
import jp.nhiguchi.libs.logpro.program.*;
import jp.nhiguchi.libs.logpro.program.term.*;
import jp.nhiguchi.libs.logpro.reasoning.sld.SLD;
import static jp.nhiguchi.libs.logpro.program.SimpleParser.*;

// 1. Build a program from facts and rules
SimpleProgramBuilder builder = new SimpleProgramBuilder();
builder.add(fact("parent(ann, cate)."));
builder.add(fact("parent(bob, cate)."));
builder.add(fact("female(ann)."));
builder.add(fact("male(bob)."));
builder.add(clause("mother(X, Y) :- parent(X, Y), female(X)."));
builder.add(clause("father(X, Y) :- parent(X, Y), male(X)."));
Program program = builder.toProgram();

// 2. Ask a query
Clause query = query("?- mother(X, cate).");

// 3. Get the first answer
Map<Variable, Term> answer = SLD.solveOne(program, query);
// => {X=ann}

// 4. Or iterate over all answers lazily
for (Map<Variable, Term> ans : SLD.solve(program, query)) {
    System.out.println(ans);
}
```

## API reference

All entry points are static methods on `SLD`:

| Method | Description |
|--------|-------------|
| `SLD.solve(program, query)` | Returns a **lazy** `Iterable` over all computed answer substitutions. Solutions are computed on demand. |
| `SLD.solveOne(program, query)` | Returns the **first** answer substitution, or `null` if none exists. |
| `SLD.solveAll(program, query)` | Returns a `Set` of **all** answer substitutions (exhausts the search). |
| `SLD.solveAll(program, query, executor)` | Same as above but evaluates SLD branches **in parallel** using the supplied `ExecutorService`. |

Each answer substitution is a `Map<Variable, Term>` mapping every query variable to its bound value.

## Parser syntax

`SimpleParser` parses Prolog-like strings:

| Input | Method | Example |
|-------|--------|---------|
| Fact | `SimpleParser.fact(s)` | `"parent(ann, cate)."` |
| Rule | `SimpleParser.clause(s)` | `"mother(X, Y) :- parent(X, Y), female(X)."` |
| Query | `SimpleParser.query(s)` | `"?- mother(X, cate)."` |
| Term  | `SimpleParser.term(s)` | `"point(x1, y1)"` |

Naming conventions:
- **Atoms / predicates** — start with a lowercase letter: `ann`, `parent`
- **Variables** — start with an uppercase letter: `X`, `Parent`
- **Compound terms** — functor with arguments: `point(x1, Y)`, `seg(P1, P2)`

## Special predicates

`Predicate.createSpecial` lets you define built-in predicates with custom evaluation logic, bypassing normal clause lookup:

```java
Predicate.Evaluable evalAtom = args -> {
    if (!(args.get(0) instanceof Atom)) return null;
    return new Predicate.Evaluable.Eval() {
        public Clause getResolvent() { return Clause.emptyQuery(); }
        public Map<Variable, ? extends Term> getMGU() { return Collections.emptyMap(); }
    };
};

Predicate atom1 = Predicate.createSpecial("atom", 1, evalAtom);
```

## Architecture overview

```
jp.nhiguchi.libs.logpro
├── program/
│   ├── term/         Term, Atom, Variable, Constant, CompoundTerm, Functor
│   ├── formula/      AtomicFormula, Predicate, Clause
│   ├── Program       (interface)
│   ├── SimpleProgram
│   ├── SimpleProgramBuilder
│   └── SimpleParser
└── reasoning/sld/
    ├── SLD           ← public API
    ├── Solver        core SLD tree expansion
    ├── Unification   MGU computation with occurs check
    ├── Resolution    single resolution step
    ├── Instances     substitution application
    ├── Variant       alpha-equivalence / variable renaming
    ├── SLDBranch     node in the SLD tree
    └── Stack         resolution substitution stack
```

## License

Released under the [MIT License](LICENSE).
