package com.compiler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.compiler.lexer.NfaMerger;
import com.compiler.lexer.TokenizedDfaSimulator;
import com.compiler.lexer.TokenizedNfaToDfaConverter;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.TokenizedNFA;
import com.compiler.lexer.regex.TokenizedRegexParser;
import com.compiler.lexer.token.Token;

/**
 * Test class for tokenized lexical analysis functionality.
 * Tests the creation, merging, and recognition of tokens using NFAs and DFAs.
 */
public class TokenTest {

    private TokenizedRegexParser parser;
    private Set<Character> alphabet;
    private Token numberToken;
    private Token identifierToken;
    private Token operatorToken;
    private Token keywordToken;

    @BeforeEach
    void setUp() {
        parser = new TokenizedRegexParser();
        
        // Define tokens with correct priority (lower ID = higher priority)
        keywordToken = new Token(1, "KEYWORD");      
        numberToken = new Token(2, "NUMBER");
        identifierToken = new Token(3, "IDENTIFIER"); 
        operatorToken = new Token(4, "OPERATOR");

        // Define alphabet
        alphabet = new HashSet<>();
        // Add digits
        for (char c = '0'; c <= '9'; c++) {
            alphabet.add(c);
        }
        // Add lowercase letters (excluding p, m, s which are operators)
        for (char c = 'a'; c <= 'z'; c++) {
            if (c != 'p' && c != 'm' && c != 's') {
                alphabet.add(c);
            }
        }
        // Add operators
        alphabet.addAll(Arrays.asList('p', 'm', 's')); // p=plus, m=minus, s=star
    }

    @Test
    void testTokenCreation() {
        assertEquals(1, keywordToken.getId());
        assertEquals("KEYWORD", keywordToken.getName());
        
        assertEquals(2, numberToken.getId());
        assertEquals("NUMBER", numberToken.getName());
        
        assertEquals(3, identifierToken.getId());
        assertEquals("IDENTIFIER", identifierToken.getName());
    }

    @Test
    void testTokenizedNFACreation() {
        String regex = "ab*";
        TokenizedNFA nfa = parser.parseWithToken(regex, keywordToken);
        
        assertNotNull(nfa);
        assertNotNull(nfa.startState);
        assertNotNull(nfa.endState);
        assertEquals(keywordToken, nfa.getToken());
        assertEquals(keywordToken, nfa.endState.getToken());
        assertTrue(nfa.endState.isFinal());
    }

    @Test
    void testNFAMerging() {
        TokenizedNFA nfa1 = parser.parseWithToken("a", keywordToken);
        TokenizedNFA nfa2 = parser.parseWithToken("b", numberToken);
        
        List<TokenizedNFA> nfas = Arrays.asList(nfa1, nfa2);
        NFA mergedNFA = NfaMerger.mergeTokenizedNfas(nfas);
        
        assertNotNull(mergedNFA);
        assertNotNull(mergedNFA.startState);
        
        // The merged NFA should have epsilon transitions to both original start states
        assertEquals(2, mergedNFA.startState.transitions.size());
    }

    @Test
    void testSingleTokenRecognition() {
        TokenizedNFA numberNFA = parser.parseWithToken("1", numberToken);
        
        Set<Character> simpleAlphabet = Set.of('1');
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(numberNFA, simpleAlphabet);
        
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        Token result = simulator.simulateForToken(dfa, "1");
        
        assertNotNull(result);
        assertEquals(numberToken.getId(), result.getId());
        assertEquals(numberToken.getName(), result.getName());
    }

    @ParameterizedTest
    @CsvSource({
        "123, NUMBER, 2",
        "456, NUMBER, 2", 
        "0, NUMBER, 2",
        "999, NUMBER, 2"
    })
    void testNumberTokenRecognition(String input, String expectedTokenName, int expectedTokenId) {
        String numberRegex = "(0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*";
        TokenizedNFA numberNFA = parser.parseWithToken(numberRegex, numberToken);
        
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(numberNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        Token result = simulator.simulateForToken(dfa, input);
        
        assertNotNull(result);
        assertEquals(expectedTokenName, result.getName());
        assertEquals(expectedTokenId, result.getId());
    }

    @ParameterizedTest
    @CsvSource({
        "abc, IDENTIFIER, 3",
        "hello, IDENTIFIER, 3",
        "x, IDENTIFIER, 3",
        "variable, IDENTIFIER, 3"
    })
    void testIdentifierTokenRecognition(String input, String expectedTokenName, int expectedTokenId) {
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|x|y|z)(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|x|y|z)*"; // Excludes p,m,s
        TokenizedNFA identifierNFA = parser.parseWithToken(identifierRegex, identifierToken);
        
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(identifierNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        Token result = simulator.simulateForToken(dfa, input);
        
        assertNotNull(result);
        assertEquals(expectedTokenName, result.getName());
        assertEquals(expectedTokenId, result.getId());
    }

    @Test
    void testMultipleTokensWithPriority() {
        // Create multiple NFAs where "if" could match both KEYWORD and IDENTIFIER
        // KEYWORD has lower ID (1) so it should have priority over IDENTIFIER (3)
        
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|x|y|z)(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|x|y|z)*"; // Excludes p,m,s
        String keywordRegex = "if|while|for";
        
        TokenizedNFA identifierNFA = parser.parseWithToken(identifierRegex, identifierToken);
        TokenizedNFA keywordNFA = parser.parseWithToken(keywordRegex, keywordToken);
        
        List<TokenizedNFA> nfas = Arrays.asList(identifierNFA, keywordNFA);
        NFA mergedNFA = NfaMerger.mergeTokenizedNfas(nfas);
        
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(mergedNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        
        // Test "if" - should be recognized as KEYWORD (lower ID wins)
        Token result = simulator.simulateForToken(dfa, "if");
        assertNotNull(result);
        assertEquals("KEYWORD", result.getName());
        assertEquals(1, result.getId());
        
        // Test "hello" - should be recognized as IDENTIFIER
        result = simulator.simulateForToken(dfa, "hello");
        assertNotNull(result);
        assertEquals("IDENTIFIER", result.getName());
        assertEquals(3, result.getId());
    }

    @Test
    void testCompleteTokenizer() {
        // Create a complete tokenizer with numbers, identifiers, operators, and keywords
        String numberRegex = "(0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*";
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|x|y|z)(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|x|y|z)*"; // Excludes p,m,s
        String operatorRegex = "p|m|s"; // p=plus, m=minus, s=star
        String keywordRegex = "if|while|for";
        
        TokenizedNFA keywordNFA = parser.parseWithToken(keywordRegex, keywordToken);
        TokenizedNFA numberNFA = parser.parseWithToken(numberRegex, numberToken);
        TokenizedNFA identifierNFA = parser.parseWithToken(identifierRegex, identifierToken);
        TokenizedNFA operatorNFA = parser.parseWithToken(operatorRegex, operatorToken);
        
        List<TokenizedNFA> nfas = Arrays.asList(keywordNFA, numberNFA, identifierNFA, operatorNFA);
        NFA mergedNFA = NfaMerger.mergeTokenizedNfas(nfas);
        
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(mergedNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        
        // Test various inputs
        assertTokenEquals(simulator.simulateForToken(dfa, "123"), "NUMBER", 2);
        assertTokenEquals(simulator.simulateForToken(dfa, "variable"), "IDENTIFIER", 3);
        assertTokenEquals(simulator.simulateForToken(dfa, "p"), "OPERATOR", 4);
        assertTokenEquals(simulator.simulateForToken(dfa, "if"), "KEYWORD", 1);
        assertTokenEquals(simulator.simulateForToken(dfa, "while"), "KEYWORD", 1);
        assertTokenEquals(simulator.simulateForToken(dfa, "m"), "OPERATOR", 4);
        
        // Test non-matching input
        assertNull(simulator.simulateForToken(dfa, "12ab"));
        assertNull(simulator.simulateForToken(dfa, ""));
        assertNull(simulator.simulateForToken(dfa, "@"));
    }

    @Test
    void testEmptyInput() {
        TokenizedNFA numberNFA = parser.parseWithToken("(0|1)(0|1)*", numberToken);
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(numberNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        
        Token result = simulator.simulateForToken(dfa, "");
        assertNull(result);
    }

    @Test 
    void testInvalidInput() {
        TokenizedNFA numberNFA = parser.parseWithToken("(0|1)(0|1)*", numberToken);
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(numberNFA, Set.of('0', '1'));
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        
        Token result = simulator.simulateForToken(dfa, "abc");
        assertNull(result);
    }

    /**
     * Helper method to assert token properties.
     */
    private void assertTokenEquals(Token actual, String expectedName, int expectedId) {
        assertNotNull(actual, "Expected token but got null");
        assertEquals(expectedName, actual.getName());
        assertEquals(expectedId, actual.getId());
    }
}