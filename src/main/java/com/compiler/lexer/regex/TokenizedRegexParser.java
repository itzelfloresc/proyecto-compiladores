package com.compiler.lexer.regex;

import com.compiler.lexer.nfa.TokenizedNFA;
import com.compiler.lexer.token.Token;

/**
 * TokenizedRegexParser
 * -------------------
 * Extends RegexParser to create TokenizedNFA instances that associate tokens with final states.
 * This is useful for lexical analysis where different patterns correspond to different token types.
 */
public class TokenizedRegexParser extends RegexParser {
    
    /**
     * Default constructor for TokenizedRegexParser.
     */
    public TokenizedRegexParser() {
        super();
    }

    /**
     * Converts an infix regular expression to a TokenizedNFA with an associated token.
     *
     * @param infixRegex The regular expression in infix notation.
     * @param token The token to associate with the final state.
     * @return The constructed TokenizedNFA.
     */
    public TokenizedNFA parseWithToken(String infixRegex, Token token) {
        // Use the parent class to parse the regex
        var nfa = super.parse(infixRegex);
        
        // Create and return a TokenizedNFA
        TokenizedNFA tokenizedNFA = new TokenizedNFA(nfa.startState, nfa.endState, token);
        tokenizedNFA.endState.isFinal = true;
        
        return tokenizedNFA;
    }
}