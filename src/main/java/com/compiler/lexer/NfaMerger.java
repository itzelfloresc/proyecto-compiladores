package com.compiler.lexer;

import java.util.List;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.TokenizedNFA;
import com.compiler.lexer.nfa.Transition;

/**
 * NfaMerger
 * ---------
 * Utility class for merging multiple NFAs into a single NFA.
 * This is useful for creating lexical analyzers that can recognize multiple token types.
 */
public class NfaMerger {

    /**
     * Default constructor for NfaMerger.
     */
    public NfaMerger() {
        // Default constructor
    }

    /**
     * Merges multiple NFAs into a single NFA.
     * Creates a new start state with epsilon transitions to all input NFA start states.
     * The resulting NFA can accept any string that would be accepted by any of the input NFAs.
     * Token information is preserved from the original TokenizedNFAs.
     *
     * @param nfas The list of NFAs to merge.
     * @return A new NFA that accepts the union of all input NFAs.
     * @throws IllegalArgumentException if the input list is null or empty.
     */
    public static NFA mergeNfas(List<? extends NFA> nfas) {
        if (nfas == null || nfas.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge empty list of NFAs");
        }

        if (nfas.size() == 1) {
            return nfas.get(0);
        }

        // Create a new start state
        State newStartState = new State();
        
        // Add epsilon transitions from the new start state to all NFA start states
        for (NFA nfa : nfas) {
            newStartState.transitions.add(new Transition(null, nfa.startState));
        }

        // The merged NFA doesn't have a single end state since each original NFA
        // keeps its own final states with their tokens
        // We'll use the first NFA's end state as a placeholder, but it won't be used
        State placeholderEndState = new State();

        return new NFA(newStartState, placeholderEndState);
    }

    /**
     * Merges multiple TokenizedNFAs into a single NFA.
     * This is a convenience method that preserves type information.
     *
     * @param tokenizedNfas The list of TokenizedNFAs to merge.
     * @return A new NFA that accepts the union of all input TokenizedNFAs.
     */
    public static NFA mergeTokenizedNfas(List<TokenizedNFA> tokenizedNfas) {
        return mergeNfas(tokenizedNfas);
    }
}