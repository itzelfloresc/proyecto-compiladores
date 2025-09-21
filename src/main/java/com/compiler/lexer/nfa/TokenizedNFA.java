package com.compiler.lexer.nfa;

import com.compiler.lexer.token.Token;

/**
 * Extends NFA to support token association with final states.
 * Used for lexical analysis where different patterns correspond to different token types.
 */
public class TokenizedNFA extends NFA {
    /**
     * The token associated with this NFA's final state.
     */
    private final Token token;

    /**
     * Constructs a new TokenizedNFA with the given start and end states and associated token.
     * @param start The initial state.
     * @param end The final (accepting) state.
     * @param token The token associated with this NFA.
     */
    public TokenizedNFA(State start, State end, Token token) {
        super(start, end);
        this.token = token;
        // Set the token on the end state
        if (end != null) {
            end.setToken(token);
        }
    }

    /**
     * Gets the token associated with this NFA.
     * @return The token.
     */
    public Token getToken() {
        return token;
    }
}