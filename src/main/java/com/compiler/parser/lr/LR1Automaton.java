package com.compiler.parser.lr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;
import com.compiler.parser.syntax.StaticAnalyzer;
/**
 * Builds the canonical collection of LR(1) items (the DFA automaton).
 * Items contain a lookahead symbol.
 */
public class LR1Automaton {
    private final Grammar grammar;
    private final List<Set<LR1Item>> states = new ArrayList<>();
    private final Map<Integer, Map<Symbol, Integer>> transitions = new HashMap<>();
    private String augmentedLeftName = null;

    public LR1Automaton(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public List<Set<LR1Item>> getStates() { return states; }
    public Map<Integer, Map<Symbol, Integer>> getTransitions() { return transitions; }

    /**
     * CLOSURE for LR(1): standard algorithm using FIRST sets to compute lookaheads for new items.
     */
    private Set<LR1Item> closure(Set<LR1Item> items) {
        // TODO: Implement the CLOSURE algorithm for a set of LR(1) items.
        // 1. Initialize a new set `closure` with the given `items`.
        // 2. Create a worklist (like a Queue or List) and add all items from `items` to it.
        // 3. Pre-calculate the FIRST sets for all symbols in the grammar.
        // 4. While the worklist is not empty:
        //    a. Dequeue an item `[A -> α • B β, a]`.
        //    b. If `B` is a non-terminal:
        //       i. For each production of `B` (e.g., `B -> γ`):
        //          - Calculate the FIRST set of the sequence `βa`. This will be the lookahead for the new item.
        //          - For each terminal `b` in FIRST(βa):
        //             - Create a new item `[B -> • γ, b]`.
        //             - If this new item is not already in the `closure` set:
        //               - Add it to `closure`.
        //               - Enqueue it to the worklist.
        // 5. Return the `closure` set.
        Set<LR1Item> closure = new HashSet<>(items);
        Queue<LR1Item> worklist = new java.util.LinkedList<>(items);
        StaticAnalyzer analyzer = new StaticAnalyzer(grammar);
        Map<Symbol, Set<Symbol>> firstSets = analyzer.getFirstSets();
        Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);

        while (!worklist.isEmpty()) {
            LR1Item item = worklist.poll();
            Symbol symbolAfterDot = item.getSymbolAfterDot();

            if (symbolAfterDot != null && symbolAfterDot.type == SymbolType.NON_TERMINAL) {
                List<Symbol> beta_a = new ArrayList<>();
                if (item.dotPosition + 1 < item.production.right.size()) {
                    beta_a.addAll(item.production.right.subList(item.dotPosition + 1, item.production.right.size()));
                }
                beta_a.add(item.lookahead);

                Set<Symbol> lookaheads = computeFirstOfSequence(beta_a, firstSets, epsilon);

                for (Production prod : grammar.getProductions()) {
                    if (prod.left.equals(symbolAfterDot)) {
                        for (Symbol b : lookaheads) {
                            LR1Item newItem = new LR1Item(prod, 0, b);
                            if (closure.add(newItem)) {
                                worklist.add(newItem);
                            }
                        }
                    }
                }
            }
        }
        return closure;
    }

    /**
     * Compute FIRST of a sequence of symbols.
     */
    private Set<Symbol> computeFirstOfSequence(List<Symbol> seq, Map<Symbol, Set<Symbol>> firstSets, Symbol epsilon) {
        // TODO: Implement the logic to compute the FIRST set for a sequence of symbols.
        // 1. Initialize an empty result set.
        // 2. If the sequence is empty, add epsilon to the result and return.
        // 3. Iterate through the symbols `X` in the sequence:
        //    a. Get `FIRST(X)` from the pre-calculated `firstSets`.
        //    b. Add all symbols from `FIRST(X)` to the result, except for epsilon.
        //    c. If `FIRST(X)` does not contain epsilon, stop and break the loop.
        //    d. If it does contain epsilon and this is the last symbol in the sequence, add epsilon to the result set.
        // 4. Return the result set.
        Set<Symbol> result = new HashSet<>();
        if (seq.isEmpty()) {
            result.add(epsilon);
            return result;
        }

        boolean allHaveEpsilon = true;
        for (Symbol symbol : seq) {
            Set<Symbol> firstOfSymbol = firstSets.get(symbol);
            if (firstOfSymbol == null) {
                result.add(symbol);
                allHaveEpsilon = false;
                break;
            }

            result.addAll(firstOfSymbol.stream().filter(s -> !s.equals(epsilon)).collect(Collectors.toSet()));

            if (!firstOfSymbol.contains(epsilon)) {
                allHaveEpsilon = false;
                break;
            }
        }

        if (allHaveEpsilon) {
            result.add(epsilon);
        }
        return result;
    }

    /**
     * GOTO for LR(1): moves dot over symbol and takes closure.
     */
    private Set<LR1Item> goTo(Set<LR1Item> state, Symbol symbol) {
        // TODO: Implement the GOTO function.
        // 1. Initialize an empty set `movedItems`.
        // 2. For each item `[A -> α • X β, a]` in the input `state`:
        //    a. If `X` is equal to the input `symbol`:
        //       - Add the new item `[A -> α X • β, a]` to `movedItems`.
        // 3. Return the `closure` of `movedItems`.
        Set<LR1Item> movedItems = new HashSet<>();
        for (LR1Item item : state) {
            Symbol symbolAfterDot = item.getSymbolAfterDot();
            if (symbol != null && symbol.equals(symbolAfterDot)) {
                movedItems.add(new LR1Item(item.production, item.dotPosition + 1, item.lookahead));
            }
        }
        return closure(movedItems);
    }

    /**
     * Build the LR(1) canonical collection: states and transitions.
     */
    public void build() {
        states.clear();
        transitions.clear();

        augmentedLeftName = grammar.getStartSymbol().name + "'";
        Symbol augmentedStart = new Symbol(augmentedLeftName, SymbolType.NON_TERMINAL);
        Production augmentedProd = new Production(augmentedStart, List.of(grammar.getStartSymbol()));
        Symbol endSymbol = new Symbol("$", SymbolType.TERMINAL);

        Set<LR1Item> initialItems = new HashSet<>();
        initialItems.add(new LR1Item(augmentedProd, 0, endSymbol));

        Set<LR1Item> I0 = closure(initialItems);
        states.add(I0);

        Queue<Integer> worklist = new java.util.LinkedList<>();
        worklist.add(0);

        Set<Symbol> allSymbols = new HashSet<>();
        allSymbols.addAll(grammar.getNonTerminals());
        allSymbols.addAll(grammar.getTerminals());
        allSymbols.removeIf(s -> s.name.equals("ε")); // Epsilon doesn't cause transitions

        while (!worklist.isEmpty()) {
            int i = worklist.poll();
            Set<LR1Item> I = states.get(i);

            for (Symbol X : allSymbols) {
                Set<LR1Item> J = goTo(I, X);
                if (!J.isEmpty()) {
                    int j = -1;
                    for (int k = 0; k < states.size(); k++) {
                        if (states.get(k).equals(J)) {
                            j = k;
                            break;
                        }
                    }

                    if (j == -1) {
                        j = states.size();
                        states.add(J);
                        worklist.add(j);
                    }

                    transitions.computeIfAbsent(i, k -> new HashMap<>()).put(X, j);
                }
            }
        }
    }

    public String getAugmentedLeftName() { return augmentedLeftName; }
}