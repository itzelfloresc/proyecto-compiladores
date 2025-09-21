package com.compiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.compiler.lexer.NfaMerger;
import com.compiler.lexer.TokenizedDfaSimulator;
import com.compiler.lexer.TokenizedNfaToDfaConverter;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.TokenizedNFA;
import com.compiler.lexer.regex.TokenizedRegexParser;
import com.compiler.lexer.token.Token;

/**
 * MainToken class for demonstrating tokenized regex processing.
 * This class shows how to:
 * 1. Create multiple TokenizedNFAs from regular expressions
 * 2. Merge them into a single NFA
 * 3. Convert to DFA while preserving token information
 * 4. Simulate the DFA to recognize tokens in input strings
 */
public class MainToken {
    
    /**
     * Default constructor for MainToken.
     */
    public MainToken() {}

    /**
     * Entry point for the tokenized automaton demo.
     * Steps:
     * 1. Define token types and their regular expressions
     * 2. Create TokenizedNFAs for each token type
     * 3. Merge NFAs into a single NFA
     * 4. Convert merged NFA to DFA
     * 5. Test various input strings to identify tokens
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Tokenized Lexical Analyzer Demo ===\n");

        // --- STEP 1: Define tokens and their patterns ---
        Token keywordToken = new Token(1, "KEYWORD");      // Highest priority (lowest ID)
        Token numberToken = new Token(2, "NUMBER");
        Token identifierToken = new Token(3, "IDENTIFIER"); // Lower priority than keywords
        Token operatorToken = new Token(4, "OPERATOR");
        Token whitespaceToken = new Token(6, "WHITESPACE");

        String keywordRegex = "if|while|for";
        String numberRegex = "(0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*";
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|w|x|y|z)((a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|w|x|y|z)|(0|1|2|3|4|5|6|7|8|9))*";
        String operatorRegex = "p|m|s"; // p=plus, m=minus, s=star (avoiding operator symbols)
        String whitespaceRegex = " "; // Simple space for whitespace

        // Define alphabet
        Set<Character> alphabet = new HashSet<>();
        // Add digits
        for (char c = '0'; c <= '9'; c++) {
            alphabet.add(c);
        }
        // Add lowercase letters
        for (char c = 'a'; c <= 'z'; c++) {
            alphabet.add(c);
        }
        // Add operators and whitespace
        alphabet.addAll(Arrays.asList('p', 'm', 's', ' ')); // p=plus, m=minus, s=star

        System.out.println("Defined tokens:");
        System.out.println("1. KEYWORD: " + keywordRegex + " (highest priority)");
        System.out.println("2. NUMBER: " + numberRegex);
        System.out.println("3. IDENTIFIER: " + identifierRegex);
        System.out.println("4. OPERATOR: " + operatorRegex + " (p=+, m=-, s=*)");
        System.out.println("6. WHITESPACE: (space character)");
        System.out.println();

        // --- STEP 2: Create TokenizedNFAs ---
        TokenizedRegexParser parser = new TokenizedRegexParser();
        
        TokenizedNFA keywordNFA = parser.parseWithToken(keywordRegex, keywordToken);
        TokenizedNFA numberNFA = parser.parseWithToken(numberRegex, numberToken);
        TokenizedNFA identifierNFA = parser.parseWithToken(identifierRegex, identifierToken);
        TokenizedNFA operatorNFA = parser.parseWithToken(operatorRegex, operatorToken);
        TokenizedNFA whitespaceNFA = parser.parseWithToken(whitespaceRegex, whitespaceToken);

        System.out.println("Created individual TokenizedNFAs");

        // --- STEP 3: Merge NFAs ---
        List<TokenizedNFA> tokenizedNFAs = Arrays.asList(keywordNFA, numberNFA, identifierNFA, operatorNFA, whitespaceNFA);
        NFA mergedNFA = NfaMerger.mergeTokenizedNfas(tokenizedNFAs);
        
        System.out.println("Merged NFAs into single NFA");

        // --- STEP 4: Convert to DFA ---
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(mergedNFA, alphabet);
        
        System.out.println("Converted merged NFA to DFA");
        System.out.println("--- DFA Structure ---");
        visualizeDfa(dfa);

        // --- STEP 5: Test input strings ---
        String[] testStrings = {
            "123",      // Should be NUMBER
            "abc",      // Should be IDENTIFIER  
            "if",       // Should be KEYWORD (now has priority)
            "while",    // Should be KEYWORD
            "for",      // Should be KEYWORD
            "p",        // Should be OPERATOR (plus)
            "m",        // Should be OPERATOR (minus)
            "s",        // Should be OPERATOR (star)
            "xyz",      // Should be IDENTIFIER
            "456",      // Should be NUMBER
            "hello",    // Should be IDENTIFIER
            "unknown",  // Should be IDENTIFIER
            "12ab",     // Should not match any token
            "",         // Empty string
            "9",        // Should be NUMBER
            "variable", // Should be IDENTIFIER
            "x",         // Should be IDENTIFIER
            "1 p 2",       //Should return three tokens
            "if x m 123", // Should return four tokens
        };

        System.out.println("=== Token Recognition Results ===");
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        
        for (String testString : testStrings) {
            List<Token> recognizedTokens = simulator.tokenize(dfa, testString);
            System.out.print("Input: '" + testString + "' -> ");
            if (!recognizedTokens.isEmpty()) {
                for (Token token : recognizedTokens) {
                    // In a real lexer, you'd often ignore whitespace tokens
                    if (!token.getName().equals("WHITESPACE")) {
                        System.out.print("[" + token.getName() + "] ");
                    }
                }
                System.out.println();
            } else {
                System.out.println("No tokens recognized");
            }
        }
    }

    /**
     * Visualizes the DFA structure, showing states, transitions, and token information.
     *
     * @param dfa The DFA to visualize.
     */
    public static void visualizeDfa(DFA dfa) {
        System.out.println("Start State: D" + dfa.startState.id);
        for (DfaState state : dfa.allStates) {
            StringBuilder sb = new StringBuilder();
            sb.append("State D").append(state.id);
            if (state.isFinal()) {
                sb.append(" (Final");
                if (state.getToken() != null) {
                    sb.append(", Token: ").append(state.getToken().getName())
                      .append(" ID:").append(state.getToken().getId());
                }
                sb.append(")");
            }
            sb.append(":");
            
            // Sort transitions by character for consistent output
            state.getTransitions().entrySet().stream()
                .sorted((e1, e2) -> Character.compare(e1.getKey(), e2.getKey()))
                .forEach(entry -> {
                    sb.append("\n  --'").append(entry.getKey()).append("'--> D").append(entry.getValue().id);
                });
            System.out.println(sb.toString());
        }
        System.out.println("------------------------\n");
    }
}