package com.compiler;

import java.util.Set;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;
/**
 * Main class for demonstrating regex to NFA, DFA conversion, minimization, and simulation.
 * This class builds an automaton from a regular expression, minimizes it, and tests several input strings.
 */
/**
 * Main class for demonstrating regex to NFA, DFA conversion, minimization, and simulation.
 */
public class Main {
    /**
     * Default constructor for Main.
     */
    public Main() {}

    /**
     * Entry point for the automaton demo.
     * Steps:
     * 1. Parse regex to NFA
     * 2. Convert NFA to DFA
     * 3. Minimize DFA
     * 4. Simulate DFA with test strings
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        // --- CONFIGURATION ---
        String regex = "a(b|c)*";
        Set<Character> alphabet = Set.of('a', 'b', 'c');
        String[] testStrings = {"a", "ab", "ac", "abbc", "acb", "", "b", "abcabc"};

        System.out.println("Testing Regex: " + regex + "\n");

        // --- STEP 1: Regex -> NFA ---
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.endState.isFinal = true;
    }
}