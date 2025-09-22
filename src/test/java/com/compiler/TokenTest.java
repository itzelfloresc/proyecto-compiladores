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
import com.compiler.lexer.token.LexicalToken;
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
    private Token whitespaceToken;

    @BeforeEach
    void setUp() {
        parser = new TokenizedRegexParser();
        
        // Define tokens with correct priority (lower ID = higher priority)
        keywordToken = new Token(1, "KEYWORD");      
        numberToken = new Token(2, "NUMBER");
        identifierToken = new Token(3, "IDENTIFIER"); 
        operatorToken = new Token(4, "OPERATOR");
        whitespaceToken = new Token(5, "WHITESPACE");

        // Define alphabet
        alphabet = new HashSet<>();
        // Add digits
        for (char c = '0'; c <= '9'; c++) {
            alphabet.add(c);
        }
        // Add lowercase letters (excluding p, m, s which are operators)
        for (char c = 'a'; c <= 'z'; c++) {
            alphabet.add(c);
        }
        // Add operators
        alphabet.addAll(Arrays.asList('p', 'm', 's', ' ')); // p=plus, m=minus, s=star, space
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
        TokenizedNFA numberNFA = parser.parseWithToken("(1)", numberToken);
        
        Set<Character> simpleAlphabet = Set.of('1');
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(numberNFA, simpleAlphabet);
        
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        List<LexicalToken> results = simulator.tokenize(dfa, "1");
        
        assertNotNull(results);
        assertEquals(1, results.size(), "Expected to find exactly one token");
        LexicalToken firstToken = results.get(0);
        assertEquals("1", firstToken.getLexeme());
        assertEquals(numberToken.getId(), firstToken.getType().getId());
        assertEquals(numberToken.getName(), firstToken.getName());
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
        List<LexicalToken> results = simulator.tokenize(dfa, input);
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(expectedTokenName, results.get(0).getName());
        assertEquals(expectedTokenId, results.get(0).getType().getId());
    }

    @ParameterizedTest
    @CsvSource({
        "abc, IDENTIFIER, 3",
        "hello, IDENTIFIER, 3",
        "x, IDENTIFIER, 3",
        "variable, IDENTIFIER, 3"
    })
    void testIdentifierTokenRecognition(String input, String expectedTokenName, int expectedTokenId) {
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*"; // Excludes p, m, s
        TokenizedNFA identifierNFA = parser.parseWithToken(identifierRegex, identifierToken);
        
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(identifierNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        List<LexicalToken> results = simulator.tokenize(dfa, input);
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(expectedTokenName, results.get(0).getName());
        assertEquals(expectedTokenId, results.get(0).getType().getId());
    }

    @Test
    void testMultipleTokensWithPriority() {
        // Create multiple NFAs where "if" could match both KEYWORD and IDENTIFIER
        // KEYWORD has lower ID (1) so it should have priority over IDENTIFIER (3)
        
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*"; // Excludes p, m, s
        String keywordRegex = "if|while|for";
        
        TokenizedNFA identifierNFA = parser.parseWithToken(identifierRegex, identifierToken);
        TokenizedNFA keywordNFA = parser.parseWithToken(keywordRegex, keywordToken);
        
        List<TokenizedNFA> nfas = Arrays.asList(identifierNFA, keywordNFA);
        NFA mergedNFA = NfaMerger.mergeTokenizedNfas(nfas);
        
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(mergedNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        
        // Test "if" - should be recognized as KEYWORD (lower ID wins)
        List<LexicalToken> results = simulator.tokenize(dfa, "if");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("KEYWORD", results.get(0).getName());
        assertEquals(1, results.get(0).getType().getId());
        
        // Test "hello" - should be recognized as IDENTIFIER
        results = simulator.tokenize(dfa, "hello");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("IDENTIFIER", results.get(0).getName());
        assertEquals(3, results.get(0).getType().getId());
    }

    @Test
    void testCompleteTokenizer() {
        // Create a complete tokenizer with numbers, identifiers, operators, and keywords
        String numberRegex = "(0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*";
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*"; // Excludes p, m, s
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
        assertSingleToken(simulator.tokenize(dfa, "123"), "NUMBER", 2);
        assertSingleToken(simulator.tokenize(dfa, "variable"), "IDENTIFIER", 3);
        assertSingleToken(simulator.tokenize(dfa, "p"), "OPERATOR", 4);
        assertSingleToken(simulator.tokenize(dfa, "if"), "KEYWORD", 1);
        assertSingleToken(simulator.tokenize(dfa, "while"), "KEYWORD", 1);
        assertSingleToken(simulator.tokenize(dfa, "m"), "OPERATOR", 4);
        
        // Test non-matching input
        // The 'tokenize' method will find "12" and "ab" as separate tokens.
        // To test if an entire string is invalid as a *single* token, we still use simulateForToken.
        // This confirms that "12ab" is not a valid single number or identifier.
        assertNull(new TokenizedDfaSimulator().simulateForToken(dfa, "12ab"));
        assertTrue(simulator.tokenize(dfa, "").isEmpty());
        
        // For an invalid character, it should be tokenized as an error
        List<LexicalToken> errorResult = simulator.tokenize(dfa, "@");
        assertLexicalTokenEquals(errorResult.get(0), "ERROR", "@");
    }

    @Test
    void testMultiTokenRecognition() {
        // Create a complete tokenizer with numbers, identifiers, operators, keywords, and whitespace
        String numberRegex = "(0|1|2|3|4|5|6|7|8|9)+";
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*"; // Excludes p, m, s
        String operatorRegex = "p|m|s";
        String keywordRegex = "if|while|for";
        String whitespaceRegex = " ";

        TokenizedNFA keywordNFA = parser.parseWithToken(keywordRegex, keywordToken);
        TokenizedNFA numberNFA = parser.parseWithToken(numberRegex, numberToken);
        TokenizedNFA identifierNFA = parser.parseWithToken(identifierRegex, identifierToken);
        TokenizedNFA operatorNFA = parser.parseWithToken(operatorRegex, operatorToken);
        TokenizedNFA whitespaceNFA = parser.parseWithToken(whitespaceRegex, whitespaceToken);

        List<TokenizedNFA> nfas = Arrays.asList(keywordNFA, numberNFA, identifierNFA, operatorNFA, whitespaceNFA);
        NFA mergedNFA = NfaMerger.mergeTokenizedNfas(nfas);

        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(mergedNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();

        // Test input with multiple tokens: "if x p 123"
        // The new tokenizer splits by whitespace, so we expect 4 tokens.
        List<LexicalToken> results = simulator.tokenize(dfa, "if x p 123");
        assertNotNull(results);
        assertEquals(4, results.size()); // "if", "x", "p", "123"
        assertLexicalTokenEquals(results.get(0), "KEYWORD", "if");
        assertLexicalTokenEquals(results.get(1), "IDENTIFIER", "x");
        assertLexicalTokenEquals(results.get(2), "OPERATOR", "p");
        assertLexicalTokenEquals(results.get(3), "NUMBER", "123");
    }

    @Test
    void testLexicalErrorHandling() {
        // Build a full DFA
        String numberRegex = "(0|1|2|3|4|5|6|7|8|9)+";
        String identifierRegex = "(a|b|c|d|e|f|g|h|i|j|k|l|n|o|q|r|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*";
        String operatorRegex = "p|m|s";
        String keywordRegex = "if|while|for";

        List<TokenizedNFA> nfas = Arrays.asList(
            parser.parseWithToken(keywordRegex, keywordToken),
            parser.parseWithToken(numberRegex, numberToken),
            parser.parseWithToken(identifierRegex, identifierToken),
            parser.parseWithToken(operatorRegex, operatorToken)
        );
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(NfaMerger.mergeTokenizedNfas(nfas), alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();

        List<LexicalToken> results = simulator.tokenize(dfa, "if 123ab p 4");
        assertEquals(4, results.size());
        assertLexicalTokenEquals(results.get(0), "KEYWORD", "if");
        assertLexicalTokenEquals(results.get(1), "ERROR", "123ab"); // This is the error token
        assertLexicalTokenEquals(results.get(2), "OPERATOR", "p");
        assertLexicalTokenEquals(results.get(3), "NUMBER", "4");
    }

    @Test
    void testEmptyInput() {
        TokenizedNFA numberNFA = parser.parseWithToken("(0|1)(0|1)*", numberToken);
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(numberNFA, alphabet);
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        assertTrue(simulator.tokenize(dfa, "").isEmpty());
    }

    @Test 
    void testInvalidInput() {
        TokenizedNFA numberNFA = parser.parseWithToken("(0|1)(0|1)*", numberToken);
        DFA dfa = TokenizedNfaToDfaConverter.convertNfaToDfaWithTokens(numberNFA, Set.of('0', '1'));
        TokenizedDfaSimulator simulator = new TokenizedDfaSimulator();
        // "abc" is not in the alphabet, so it should be tokenized as an error
        List<LexicalToken> results = simulator.tokenize(dfa, "abc");
        assertEquals(1, results.size());
        assertLexicalTokenEquals(results.get(0), "ERROR", "abc");
    }

    private void assertSingleToken(List<LexicalToken> actual, String expectedName, int expectedId) {
        assertNotNull(actual, "Expected token list but was null");
        assertEquals(1, actual.size(), "Expected exactly one token");
        LexicalToken lexicalToken = actual.get(0);
        assertEquals(expectedName, lexicalToken.getName());
        assertEquals(expectedId, lexicalToken.getType().getId());
    }

    private void assertLexicalTokenEquals(LexicalToken actual, String expectedName, String expectedLexeme) {
        assertNotNull(actual, "Expected lexical token but was null");
        assertEquals(expectedName, actual.getName());
        assertEquals(expectedLexeme, actual.getLexeme());
    }
}