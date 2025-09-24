package com.compiler.parser.syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * Main task of Practice 5.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<Symbol, Set<Symbol>> followSets;
    private boolean firstSetsCalculated = false;

    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * @return A map from Symbol to its FIRST set.
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        if (firstSetsCalculated) {
            return firstSets;
        }

        // 1. Initialize FIRST sets
        // For terminals, FIRST(T) = {T}
        grammar.getTerminals().forEach(t -> firstSets.put(t, Set.of(t)));
        // For non-terminals, FIRST(A) = {}
        grammar.getNonTerminals().forEach(nt -> firstSets.put(nt, new HashSet<>()));
        // Handle epsilon separately
        firstSets.put(new Symbol("ε", SymbolType.TERMINAL), Set.of(new Symbol("ε", SymbolType.TERMINAL)));

        // 2. Repeat until no changes
        boolean changed = true;
        while (changed) {
            changed = false;
            // For each production A -> X1 X2 ... Xn
            for (Production p : grammar.getProductions()) {
                Symbol A = p.getLeft();
                Set<Symbol> firstA = firstSets.get(A);

                // Calculate FIRST of the right-hand side
                Set<Symbol> firstRhs = calculateFirstForSequence(p.getRight());

                if (firstA.addAll(firstRhs)) {
                    changed = true;
                }
            }
        }

        firstSetsCalculated = true;
        return firstSets;
    }

    /**
     * Calculates the FIRST set for a sequence of symbols (e.g., the right side of a production).
     * @param sequence The list of symbols.
     * @return The FIRST set for the sequence.
     */
    private Set<Symbol> calculateFirstForSequence(java.util.List<Symbol> sequence) {
        Set<Symbol> result = new HashSet<>();
        Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);

        if (sequence.isEmpty() || (sequence.size() == 1 && sequence.get(0).equals(epsilon))) {
            result.add(epsilon);
            return result;
        }

        boolean allHaveEpsilon = true;
        for (Symbol symbol : sequence) {
            Set<Symbol> firstSymbol = firstSets.get(symbol);
            if (firstSymbol == null) { // Should not happen in a valid grammar
                continue;
            }

            // Add FIRST(symbol) - {ε} to result
            for (Symbol s : firstSymbol) {
                if (!s.equals(epsilon)) {
                    result.add(s);
                }
            }

            // If ε is not in FIRST(symbol), we stop
            if (!firstSymbol.contains(epsilon)) {
                allHaveEpsilon = false;
                break;
            }
        }

        // If ε was in FIRST of all symbols in the sequence, add ε to the result
        if (allHaveEpsilon) {
            result.add(epsilon);
        }

        return result;
    }

    /**
     * Calculates and returns the FOLLOW sets for non-terminals.
     * @return A map from Symbol to its FOLLOW set.
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        // Ensure FIRST sets are calculated
        getFirstSets();

        // 1. Initialize FOLLOW sets for all non-terminals
        grammar.getNonTerminals().forEach(nt -> followSets.put(nt, new HashSet<>()));

        // 2. Add $ (end of input) to FOLLOW(S)
        Symbol endOfInput = new Symbol("$", SymbolType.TERMINAL);
        followSets.get(grammar.getStartSymbol()).add(endOfInput);

        // 3. Repeat until no changes
        boolean changed = true;
        while (changed) {
            changed = false;

            // For each production B -> X1 X2 ... Xn
            for (Production p : grammar.getProductions()) {
                Symbol B = p.getLeft();
                java.util.List<Symbol> rhs = p.getRight();

                for (int i = 0; i < rhs.size(); i++) {
                    Symbol Xi = rhs.get(i);

                    // Rule only applies to non-terminals
                    if (Xi.type == SymbolType.NON_TERMINAL) {
                        Set<Symbol> followXi = followSets.get(Xi);
                        int originalSize = followXi.size();

                        // Get the sequence of symbols after Xi (beta)
                        java.util.List<Symbol> beta = new ArrayList<>(rhs.subList(i + 1, rhs.size()));
                        Set<Symbol> firstOfBeta = calculateFirstForSequence(beta);

                        // a. Add FIRST(beta) - {ε} to FOLLOW(Xi)
                        Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);
                        for (Symbol s : firstOfBeta) {
                            if (!s.equals(epsilon)) {
                                followXi.add(s);
                            }
                        }

                        // b. If ε is in FIRST(beta) (or beta is empty), add FOLLOW(B) to FOLLOW(Xi)
                        if (firstOfBeta.contains(epsilon)) {
                            followXi.addAll(followSets.get(B));
                        }

                        if (followXi.size() > originalSize) {
                            changed = true;
                        }
                    }
                }
            }
        }

        return followSets;
    }
}