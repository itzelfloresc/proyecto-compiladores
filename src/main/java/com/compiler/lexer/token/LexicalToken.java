package com.compiler.lexer.token;

/**
 * Represents a recognized token from the input stream.
 * It contains the token definition (type) and the actual string value (lexeme).
 */
public class LexicalToken {
    private final Token type;
    private final String lexeme;

    /**
     * Constructs a new LexicalToken.
     *
     * @param type The token definition (e.g., NUMBER, IDENTIFIER). Can be null for error tokens.
     * @param lexeme The actual string value from the input (e.g., "123", "myVar").
     */
    public LexicalToken(Token type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    public Token getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public String getName() {
        return type != null ? type.getName() : "ERROR";
    }

    @Override
    public String toString() {
        if (type != null) {
            return "['" + lexeme + "' -> " + type.getName() + "]";
        }
        return "['" + lexeme + "' -> ERROR]";
    }
}