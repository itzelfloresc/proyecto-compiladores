package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;

import java.util.ArrayList;
import java.util.List;
import com.compiler.lexer.token.LexicalToken;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.token.Token;

/**
 * TokenizedDfaSimulator
 * --------------------
 * Simulator for running input strings on a DFA and returning token information.
 * Extends the basic DFA simulation to return the associated token when a string is accepted.
 */
public class TokenizedDfaSimulator {

    /**
     * Default constructor for TokenizedDfaSimulator.
     */
    public TokenizedDfaSimulator() {
        // Default constructor
    }

    /**
     * Scans an input string, splits it by whitespace, and tokenizes each part.
     * If a part is not a valid token, it's marked as an error.
     *
     * @param dfa The DFA to use for token recognition.
     * @param input The input string to tokenize.
     * @return A list of recognized lexical tokens (including errors).
     */
    public List<LexicalToken> tokenize(DFA dfa, String input) {
        List<LexicalToken> tokens = new ArrayList<>();
        if (dfa == null || input == null || input.isEmpty()) {
            return tokens;
        }

        // Split the input string by one or more whitespace characters
        String[] words = input.trim().split("\\s+");

        for (String word : words) {
            // After trim() and split(), an input of only whitespace can result in a single empty string.
            if (word.isEmpty() && words.length == 1) {
                continue;
            }

            if (word.isEmpty()) continue;
            // Use simulateForToken to check if the entire word is a valid token
            Token tokenType = simulateForToken(dfa, word);
            tokens.add(new LexicalToken(tokenType, word));
        }

        return tokens;
    }

    /**
     * Simulates the DFA on the given input string and returns the associated token.
     * Starts at the DFA's start state and processes each character, following transitions.
     * If a transition does not exist for a character, the input is rejected.
     *
     * @param dfa The DFA to simulate.
     * @param input The input string to test.
     * @return The token associated with the final state if the input is accepted, null otherwise.
     */
    public Token simulateForToken(DFA dfa, String input) {
        // Validación de entrada
        if (dfa == null || input == null) {
            return null;
        }
        
        // 1. Inicializar el estado actual con el estado inicial del DFA
        DfaState currentState = dfa.startState;
        
        // 2. Procesar cada carácter de la cadena de entrada
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            
            // 3. Obtener el siguiente estado usando la transición para el carácter actual
            DfaState nextState = currentState.getTransition(currentChar);
            
            // 4. Si no existe transición para este carácter, rechazar la entrada
            if (nextState == null) {
                return null;
            }
            
            // 5. Actualizar el estado actual al siguiente estado
            currentState = nextState;
        }
        
        // 6. Después de procesar todos los caracteres, verificar si el estado actual es final
        if (currentState.isFinal()) {
            return currentState.getToken();
        } else {
            return null;
        }
    }

    /**
     * Simulates the DFA on the given input string and returns whether it's accepted.
     * This is a convenience method for backwards compatibility.
     *
     * @param dfa The DFA to simulate.
     * @param input The input string to test.
     * @return True if the input is accepted by the DFA, false otherwise.
     */
    public boolean simulate(DFA dfa, String input) {
        return simulateForToken(dfa, input) != null;
    }
}