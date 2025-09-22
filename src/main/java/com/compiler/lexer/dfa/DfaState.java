package com.compiler.lexer.dfa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.compiler.lexer.nfa.State;
import com.compiler.lexer.token.Token;

/**
 * DfaState
 * --------
 * Represents a single state in a Deterministic Finite Automaton (DFA).
 * Each DFA state corresponds to a set of states from the original NFA.
 * Provides methods for managing transitions, checking finality, token association, and equality based on NFA state sets.
 */
public class DfaState {
    /**
     * Returns all transitions from this state.
     * @return Map of input symbols to destination DFA states.
     */
    public Map<Character, DfaState> getTransitions() {
        // TODO: Implement getTransitions
        return this.transitions;
    }
    private static int nextId = 0;
    /**
     * Unique identifier for this DFA state.
     */
    public final int id;
    /**
     * The set of NFA states this DFA state represents.
     */
    public final Set<State> nfaStates;
    /**
     * Indicates whether this DFA state is a final (accepting) state.
     */
    public boolean isFinal;
    /**
     * Map of input symbols to destination DFA states (transitions).
     */
    public final Map<Character, DfaState> transitions;
    
    /**
     * The token associated with this DFA state (if final).
     */
    private Token token;

    /**
     * Constructs a new, empty DFA state for the minimization process.
     */
    public DfaState() {
        this.id = nextId++;
        this.nfaStates = new HashSet<>();
        this.isFinal = false;
        this.transitions = new HashMap<>();
        this.token = null;
        
        // Check if any NFA state has a token and set it
        updateTokenFromNfaStates();
    }

    /**
     * Constructs a new DFA state.
     * @param nfaStates The set of NFA states that this DFA state represents.
     */
    public DfaState(Set<State> nfaStates) {
        // TODO: Implement constructor
        this.id = nextId++;
        this.nfaStates = nfaStates;
        this.isFinal = false;
        this.transitions = new HashMap<>();
        this.token = null;
        
        // Check if any NFA state has a token and set it
        updateTokenFromNfaStates();
    }

    /**
     * Updates the token based on the NFA states this DFA state represents.
     * If multiple tokens are found, the one with the lowest ID takes precedence.
     */
    private void updateTokenFromNfaStates() {
        Token selectedToken = null;
        for (State nfaState : nfaStates) {
            Token nfaToken = nfaState.getToken();
            if (nfaToken != null) {
                if (selectedToken == null || nfaToken.getId() < selectedToken.getId()) {
                    selectedToken = nfaToken;
                }
            }
        }
        this.token = selectedToken;
        if (this.token != null) {
            this.isFinal = true;
        }
    }

    /**
     * Adds a transition from this state to another on a given symbol.
     * @param symbol The input symbol for the transition.
     * @param toState The destination DFA state.
     */
    public void addTransition(Character symbol, DfaState toState) {
        // TODO: Implement addTransition
        this.transitions.put(symbol, toState);
    }

    /**
     * Two DfaStates are considered equal if they represent the same set of NFA states.
     * @param obj The object to compare.
     * @return True if the states are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        // TODO: Implement equals
        if(obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        if(this == obj) {
            return true;
        }
        DfaState dfaState = (DfaState) obj;
        return Objects.equals(this.nfaStates, dfaState.nfaStates);
    }

    /**
     * The hash code is based on the set of NFA states.
     * @return The hash code for this DFA state.
     */
    @Override
    public int hashCode() {
        // TODO: Implement hashCode
        return Objects.hash(this.nfaStates);
    }
    
    /**
     * Returns a string representation of the DFA state, including its id, finality, and token.
     * @return String representation of the state.
     */
    @Override
    public String toString() {
        // TODO: Implement toString
        return ("DFA state = {id: " + this.id + ", isFinal: " + this.isFinal + 
                ", token: " + this.token + ", nfaStates: (" + this.nfaStates + ")}");
    }

    /**
     * Sets the finality of the DFA state.
     * @param isFinal True if this state is a final state, false otherwise.
     */
    public void setFinal(boolean isFinal) {
        // TODO: Implement setFinal
        this.isFinal = isFinal;
    }

    /**
     * Checks if the DFA state is final.
     * @return True if this state is a final state, false otherwise.
     */
    public boolean isFinal() {
        // TODO: Implement isFinal
        return this.isFinal;
    }

    /**
     * Gets the transition for a given input symbol.
     * @param symbol The input symbol for the transition.
     * @return The destination DFA state for the transition, or null if there is no transition for the given symbol.
     */
    public DfaState getTransition(char symbol) {
        // TODO: Implement getTransition
        return this.transitions.get(symbol);
    }

    /**
     * Returns the set of NFA states this DFA state represents.
     * @return The set of NFA states.
     */
    public Set<State> getName() {
        // TODO: Implement getName
        return this.nfaStates;
    }

    /**
     * Gets the token associated with this DFA state.
     * @return The token, or null if no token is associated.
     */
    public Token getToken() {
        return this.token;
    }

    /**
     * Sets the token associated with this DFA state.
     * @param token The token to associate with this state.
     */
    public void setToken(Token token) {
        this.token = token;
    }
}