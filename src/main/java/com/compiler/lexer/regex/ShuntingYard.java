package com.compiler.lexer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 * <p>
 * Provides methods to preprocess regular expressions by inserting explicit
 * concatenation operators, and to convert infix regular expressions to postfix
 * notation for easier parsing and NFA construction.
 */
/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 */
public class ShuntingYard {

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // TODO: Implement constructor if needed
    }

    /**
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        // TODO: Implement insertConcatenationOperator
        /*
            Pseudocode:
            For each character in regex:
                - Append current character to output
                - If not at end of string:
                        - Check if current and next character form an implicit concatenation
                        - If so, append '·' to output
            Return output as string
         */
        if (regex == null || regex.isEmpty()) {
            return regex;
        }

        StringBuilder concatRegex = new StringBuilder();
        char[] chars = regex.toCharArray();

        for (int i = 0; i < chars.length; i++) {

            if (i == chars.length -1) {
                concatRegex.append(chars[i]);
                continue;
            }

            char current = chars[i];
            char next = chars[i+1];

            boolean currentSiConcatena = (Character.isLetter(current) || current == '*' ||
                                          current == '+' || current == ')' || current == '}' ||
                                          current == '?');
            boolean nextSiConcatena = (Character.isLetter(next) || next == '(');

            if (currentSiConcatena && nextSiConcatena) {
                concatRegex.append(current);
                concatRegex.append('·');
            } else {
                concatRegex.append(current);
            }
        }

        return concatRegex.toString();
    }

    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    private static boolean isOperand(char c) {
        // TODO: Implement isOperand
        /*
        Pseudocode:
        Return true if c is not one of: '|', '*', '?', '+', '(', ')', '·'
         */
        return (c != '|' && c != '*' && c != '?' && c != '(' && c != ')' && c != '·' && c != '+');
    }

    /**
     * Converts an infix regular expression to postfix notation using the
     * Shunting Yard algorithm. This is useful for constructing NFAs from
     * regular expressions.
     *
     * @param infixRegex Regular expression in infix notation.
     * @return Regular expression in postfix notation.
     */
    public static String toPostfix(String infixRegex) {
        // TODO: Implement toPostfix
        /*
        Pseudocode:
        1. Define operator precedence map
        2. Preprocess regex to insert explicit concatenation operators
        3. For each character in regex:
            - If operand: append to output
            - If '(': push to stack
            - If ')': pop operators to output until '(' is found
            - If operator: pop operators with higher/equal precedence, then push current operator
        4. After loop, pop remaining operators to output
        5. Return output as string
         */
        Map<Character, Integer> precedence = new HashMap<>();
        precedence.put('|', 1);
        precedence.put('·', 2);
        precedence.put('*', 3);
        precedence.put('+', 3);
        precedence.put('?', 3);


        String infix = insertConcatenationOperator(infixRegex);
        char[] charsInfix = infix.toCharArray();

        StringBuilder postfix = new StringBuilder();
        Stack<Character> stack = new Stack<>(); 

        for (char c : charsInfix){
            if (isOperand(c)) {
                postfix.append(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')'){
                while (!stack.isEmpty() && stack.peek() != '(') {
                    postfix.append(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek() == '(') {
                    stack.pop();
                }
            } else {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    char atTop = stack.peek();
                    int preceTop = precedence.getOrDefault(atTop, -1);
                    int preceCurrent = precedence.getOrDefault(c, -1);

                    boolean isUnario = (c == '*' || c == '+' || c == '?');

                    if (preceTop > preceCurrent || (preceTop == preceCurrent && !isUnario)) {
                        postfix.append(stack.pop());
                    } else {
                        break;
                    }
                }
                stack.push(c);
            }
        }
        while(!stack.isEmpty()) {
            postfix.append(stack.pop());
        }
        return postfix.toString();
    }
}