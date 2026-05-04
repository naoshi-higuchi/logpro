# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**logpro** is a Java library implementing **SLD resolution** (Selective Linear Definite clause resolution) with **unification**, a core technique from logic programming. The library enables solving logic programs by finding computed answer substitutions (variable bindings) that satisfy queries against a set of facts and rules.

## Build System

The project uses **Maven** for dependency management and building.

### Common Commands

- **Build the project:** `mvn clean package`
- **Run all tests:** `mvn test`
- **Run a specific test class:** `mvn test -Dtest=SampleCodeTest` (or any test class name)
- **Run tests matching a pattern:** `mvn test -Dtest=Unification*`
- **Compile only:** `mvn compile`
- **View dependencies:** `mvn dependency:tree`

**Configuration:** Java 1.7 source/target via maven-compiler-plugin in pom.xml. Output is packaged as a jar.

### Dependencies

The library depends on three custom internal libraries that are **not on Maven Central** and must be installed locally first:
- `jp.nhiguchi.libs:tuple` (v0.1) - Tuple data structures (`../tuple.git`)
- `jp.nhiguchi.libs:flist` (v0.1) - Functional list implementation (`../flist.git`)
- `jp.nhiguchi.libs:pcom` (v0.1) - Parser combinator library (source location unknown)

Install each sibling dependency via `mvn install` in its directory before building logpro.

Test dependency: JUnit 4.13.1

## Architecture Overview

The project is organized into two main packages:

### 1. `jp.nhiguchi.libs.logpro.program` - Program Representation

Defines the data structures for logic programs (facts, rules, queries).

**Key components:**
- **Term hierarchy** (`program/term/`):
  - `Term` (marker interface) - Base for all logical terms
  - `Atom` - Atomic values (e.g., `ann`, `bob`)
  - `Variable` - Logical variables (uppercase, e.g., `X`, `Y`)
  - `Constant` - Atomic constants
  - `CompoundTerm` - Functor with arguments (e.g., `parent(ann, bob)`)
  - `Functor` - Predicate functor with arity

- **Formula hierarchy** (`program/formula/`):
  - `AtomicFormula` - A predicate applied to terms (e.g., `parent(X, Y)`)
  - `Predicate` - Represents a predicate name and arity; can be regular or "special" (with custom evaluation)
  - `Clause` - Immutable representation of facts (head only), rules (head :- body), or queries (?- body)

- **Program interface & implementations** (`program/`):
  - `Program` - Interface returning clauses for a given literal (query predicate)
  - `SimpleProgram` - Immutable in-memory implementation indexed by predicate
  - `SimpleProgramBuilder` - Fluent builder for constructing programs
  - `SimpleParser` - Parser for Prolog-like syntax: facts, rules, queries, terms

**Parser capabilities:**
Uses parser combinators (from pcom library) to parse:
- Facts: `parent(ann, cate).`
- Rules: `mother(X, Y) :- parent(X, Y), female(X).`
- Queries: `?- mother(X, cate).`
- Terms: atoms, variables, compound terms

### 2. `jp.nhiguchi.libs.logpro.reasoning.sld` - SLD Resolution & Solving

Implements the core SLD resolution algorithm and unification for query solving.

**Key components:**
- **SLD** - Public API for solving:
  - `solve(Program, Clause)` - Returns iterable of all computed answer substitutions (CAS)
  - `solveOne(Program, Clause)` - Returns first answer or null
  - `solveAll(Program, Clause)` - Returns set of all answers
  - `solveAll(Program, Clause, ExecutorService)` - Parallel solving with thread pool

- **Solver** - Core algorithm:
  - Manages SLD tree expansion and solution extraction
  - Implements iterative deepening with resolution steps
  - Computes answer substitutions from resolution chains

- **Unification** - Unification algorithm:
  - `getMGU(AtomicFormula, AtomicFormula)` - Computes most general unifier (MGU)
  - Implements occurs check to prevent infinite structures
  - Returns variable-to-term bindings or null if unification fails

- **Resolution** - Resolution step:
  - Selects a goal, finds matching clauses, applies unification
  - Produces new goals by replacing selected goal with rule body

- **Supporting structures**:
  - `SLDBranch` - Branch node in SLD tree (goal + resolution stack)
  - `Stack` - Resolution stack tracking substitutions
  - `Goal` - Wrapper for query clause during resolution
  - `Instances` - Applies substitutions to terms/formulas
  - `Variant` - Checks alpha-equivalence and variable renaming
  - `BlockingLIFOQueue`, `Resolver`, `Util` - Implementation details

## Typical Usage Pattern

```java
// 1. Build a program from facts and rules
SimpleProgramBuilder builder = new SimpleProgramBuilder();
builder.add(SimpleParser.fact("parent(ann, cate)."));
builder.add(SimpleParser.fact("female(ann)."));
builder.add(SimpleParser.clause("mother(X, Y) :- parent(X, Y), female(X)."));
Program program = builder.toProgram();

// 2. Parse a query
Clause query = SimpleParser.query("?- mother(X, cate).");

// 3. Solve and retrieve answers
Map<Variable, Term> answer = SLD.solveOne(program, query);
// or iterate through all answers
for (Map<Variable, Term> answer : SLD.solve(program, query)) {
    System.out.println(answer);
}
```

See `SampleCodeTest` and `SolverTest` for complete examples.

## Test Organization

Tests are in `src/test/java` parallel to main code structure:
- `SampleCodeTest` - Simple family relationships example
- `SolverTest` - Comprehensive solver behavior (family tree, geometry examples)
- `UnificationTest` - Unification algorithm specifics
- `ResolutionTest` - Resolution step behavior
- `SLDTest` - SLD algorithm properties
- `StackTest` - Resolution stack management
- `SimpleParserTest` - Parser validation
- `AtomTest` - Atom implementation

**Note:** Tests use JUnit 4. No test runner configuration in pom.xml; Maven runs them automatically.

## Key Design Insights

1. **Immutability:** Program, Clause, Term, Formula implementations are immutable, supporting safe concurrent use.

2. **Parser combinators:** SimpleParser uses composition of small parsers to build complex ones, making it modular and testable.

3. **Special predicates:** `Predicate.createSpecial()` allows custom evaluation logic beyond standard SLD (useful for built-in predicates like `atom/1`, `match/2`).

4. **Computed answer substitutions (CAS):** The public API returns answer as `Map<Variable, Term>`, representing all variable bindings needed to satisfy the query.

5. **Cut operator:** AtomicFormula.CUT represents the `!` operator; handled specially in clause ordering (SimpleProgramBuilder skips sorting when clauses contain cuts).

6. **FList:** Uses functional list library (immutable linked lists) for efficient persistent data structures in SLD tree.

## Development Notes

- The `reasoning.sld` package is tightly integrated; changes to unification or resolution propagate through Solver and SLD.
- Parser uses regex-based term/variable/predicate name validation (case-sensitive: uppercase for variables, lowercase for atoms/predicates).
- Parallel solving via ExecutorService is available but requires careful handling of shared mutable state in trees.

