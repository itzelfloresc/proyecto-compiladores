package com.compiler.lexer.regex;

import java.util.Stack;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

/**
 * RegexParser
 * -----------
 * This class provides functionality to convert infix regular expressions into nondeterministic finite automata (NFA)
 * using Thompson's construction algorithm. It supports standard regex operators: concatenation (·), union (|),
 * Kleene star (*), optional (?), and plus (+). The conversion process uses the Shunting Yard algorithm to transform
 * infix regex into postfix notation, then builds the corresponding NFA.
 *
 * Features:
 * - Parses infix regular expressions and converts them to NFA.
 * - Supports regex operators: concatenation, union, Kleene star, optional, plus.
 * - Implements Thompson's construction rules for NFA generation.
 *
 * Example usage:
 * <pre>
 *     RegexParser parser = new RegexParser();
 *     NFA nfa = parser.parse("a(b|c)*");
 * </pre>
 */
/**
 * Parses regular expressions and constructs NFAs using Thompson's construction.
 */
public class RegexParser {
    /**
     * Default constructor for RegexParser.
     */
        public RegexParser() {
            // TODO: Implement constructor if needed
        }

    /**
     * Converts an infix regular expression to an NFA.
     *
     * @param infixRegex The regular expression in infix notation.
     * @return The constructed NFA.
     */
    public NFA parse(String infixRegex) {
        // TODO: Implement parse
        // Pseudocode: Convert infix to postfix, then build NFA from postfix
        String postfixRegex = ShuntingYard.toPostfix(infixRegex);
        return buildNfaFromPostfix(postfixRegex);
    }

    /**
     * Builds an NFA from a postfix regular expression.
     *
     * @param postfixRegex The regular expression in postfix notation.
     * @return The constructed NFA.
     */
    private NFA buildNfaFromPostfix(String postfixRegex) {
        // TODO: Implement buildNfaFromPostfix
        // Pseudocode: For each char in postfix, handle operators and operands using a stack
        Stack<NFA> stack = new Stack<>();

        char[] chars = postfixRegex.toCharArray();

        for (char c : chars) {
            if (isOperand(c)) {
                NFA nfa = createNfaForCharacter(c);
                stack.push(nfa);
            } else {
                switch (c) {
                    case '.':
                    case '·':
                        handleConcatenation(stack);
                        break;
                    case '|':
                        handleUnion(stack);
                        break;
                    case '*':
                        handleKleeneStar(stack);
                        break;
                    case '?':
                        handleOptional(stack);
                        break;
                    case '+':
                        handlePlus(stack);
                        break;
                    default:
                        throw new IllegalArgumentException("Operador desconocido: " + c);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Expresión postfija inválida, el stack contiene " + stack.size() + " elementos");
        }

        return stack.pop();
    }

    /**
     * Handles the '?' operator (zero or one occurrence).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or one occurrence.
     * @param stack The NFA stack.
     */
    private void handleOptional(Stack<NFA> stack) {
        // TODO: Implement handleOptional
        // Pseudocode: Pop NFA, create new start/end, add epsilon transitions for zero/one occurrence
        if (stack.isEmpty()) {
            throw new IllegalStateException("El stack de NFAs está vacío, no se puede aplicar el operador ?");
        }
        
        NFA nfa = stack.pop();
        
        State newStartState = new State();
        State newEndState = new State();

        newStartState.transitions.add(new Transition(null, newEndState));
        newStartState.transitions.add(new Transition(null, nfa.startState));
        
        nfa.endState.isFinal = false;
        nfa.endState.transitions.add(new Transition(null, newEndState));
        
        NFA resultNFA = new NFA(newStartState, newEndState);
        
        stack.push(resultNFA);
    }

    /**
     * Handles the '+' operator (one or more occurrences).
     * Pops an NFA from the stack and creates a new NFA that accepts one or more occurrences.
     * @param stack The NFA stack.
     */
    private void handlePlus(Stack<NFA> stack) {
        // TODO: Implement handlePlus
        // Pseudocode: Pop NFA, create new start/end, add transitions for one or more occurrence
        if (stack.isEmpty()) {
            throw new IllegalStateException("El stack de NFAs está vacío, no se puede aplicar el operador +");
        }
        
        NFA nfa = stack.pop();
        
        State newStartState = new State();
        State newEndState = new State();
        
        newStartState.transitions.add(new Transition(null, nfa.startState));
        
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        
        nfa.endState.transitions.add(new Transition(null, newEndState));
        
        NFA resultNFA = new NFA(newStartState, newEndState);
        
        stack.push(resultNFA);
    }
    
    /**
     * Creates an NFA for a single character.
     * @param c The character to create an NFA for.
     * @return The constructed NFA.
     */
    private NFA createNfaForCharacter(char c) {
     // TODO: Implement createNfaForCharacter
        // Pseudocode: Create start/end state, add transition for character
        State startState = new State();
        State endState = new State();
        
        startState.transitions.add(new Transition(c, endState));
        
        NFA nfa = new NFA(startState, endState);
        
        return nfa;
    }

    /**
     * Handles the concatenation operator (·).
     * Pops two NFAs from the stack and connects them in sequence.
     * @param stack The NFA stack.
     */
    private void handleConcatenation(Stack<NFA> stack) {
        // TODO: Implement handleConcatenation
        // Pseudocode: Pop two NFAs, connect end of first to start of second
        if (stack.size() < 2) {
            throw new IllegalStateException("Stack tiene menos de dos NFAs, no se puede hacer concatenación");
        }
        
        NFA secondNfa = stack.pop();
        NFA firstNfa = stack.pop();
        
        firstNfa.endState.transitions.add(new Transition(null, secondNfa.startState));
        
        NFA resultNFA = new NFA(firstNfa.startState, secondNfa.endState);
        
        stack.push(resultNFA);
    }

    /**
     * Handles the union operator (|).
     * Pops two NFAs from the stack and creates a new NFA that accepts either.
     * @param stack The NFA stack.
     */
    private void handleUnion(Stack<NFA> stack) {
        // TODO: Implement handleUnion
        // Pseudocode: Pop two NFAs, create new start/end, add epsilon transitions for union
        if (stack.size() < 2) {
            throw new IllegalStateException("Stack tiene menos de dos NFAs, no se puede hacer unión");
        }
        
        NFA secondNfa = stack.pop();
        NFA firstNfa = stack.pop();
        
        State newStartState = new State();
        State newEndState = new State();
        
        newStartState.transitions.add(new Transition(null, firstNfa.startState));
        newStartState.transitions.add(new Transition(null, secondNfa.startState));
        
        firstNfa.endState.transitions.add(new Transition(null, newEndState));
        secondNfa.endState.transitions.add(new Transition(null, newEndState));
        
        NFA resultNFA = new NFA(newStartState, newEndState);
        
        stack.push(resultNFA);
    }

    /**
     * Handles the Kleene star operator (*).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or more repetitions.
     * @param stack The NFA stack.
     */
    private void handleKleeneStar(Stack<NFA> stack) {
        // TODO: Implement handleKleeneStar
        // Pseudocode: Pop NFA, create new start/end, add transitions for zero or more repetitions
        if (stack.isEmpty()) {
            throw new IllegalStateException("El stack de NFAs está vacío, no se puede aplicar el operador *");
        }
        
        NFA nfa = stack.pop();
        
        State newStartState = new State();
        State newEndState = new State();
        
        newStartState.transitions.add(new Transition(null, newEndState));
        newStartState.transitions.add(new Transition(null, nfa.startState));
        
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        nfa.endState.transitions.add(new Transition(null, newEndState));
        
        NFA resultNFA = new NFA(newStartState, newEndState);
        
        stack.push(resultNFA);
    }

    /**
     * Checks if a character is an operand (not an operator).
     * @param c The character to check.
     * @return True if the character is an operand, false if it is an operator.
     */
    private boolean isOperand(char c) {
        // TODO: Implement isOperand
        // Pseudocode: Return true if c is not an operator
        return (c != '|' && c != '*' && c != '?' && c != '(' && c != ')' && c != '·' && c != '+');
    }
}