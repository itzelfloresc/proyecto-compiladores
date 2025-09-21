package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;

import java.util.ArrayList;
import java.util.List;
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
     * Scans an input string and breaks it down into a sequence of tokens.
     * It uses the "maximal munch" principle, finding the longest possible valid token at each position.
     *
     * @param dfa The DFA to use for token recognition.
     * @param input The input string to tokenize.
     * @return A list of recognized tokens.
     */
    public List<Token> tokenize(DFA dfa, String input) {
        List<Token> tokens = new ArrayList<>();
        if (dfa == null || input == null || input.isEmpty()) {
            return tokens;
        }

        int currentIndex = 0;
        while (currentIndex < input.length()) {
            DfaState currentState = dfa.startState;
            int lastAcceptedIndex = -1;
            Token lastAcceptedToken = null;

            // Find the longest match from the current position
            for (int i = currentIndex; i < input.length(); i++) {
                char currentChar = input.charAt(i);
                DfaState nextState = currentState.getTransition(currentChar);

                if (nextState == null) {
                    break; // No more transitions, stop searching for this token
                }

                currentState = nextState;

                if (currentState.isFinal()) {
                    lastAcceptedIndex = i;
                    lastAcceptedToken = currentState.getToken();
                }
            }

            if (lastAcceptedToken != null) {
                // We found a token
                tokens.add(lastAcceptedToken);
                currentIndex = lastAcceptedIndex + 1;
            } else {
                // No token recognized at the current position, skip the character and continue
                // In a real compiler, you would report an error here.
                currentIndex++;
            }
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