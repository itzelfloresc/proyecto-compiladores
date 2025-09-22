package com.compiler.lexer.token;

/**
 * Represents a token with an identifier and name.
 * Used to associate final states with specific token types.
 */
public class Token {
    private final int id;
    private final String name;

    /**
     * Constructs a new Token.
     * @param id The unique identifier for this token.
     * @param name The name/type of this token.
     */
    public Token(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the token ID.
     * @return The token identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the token name.
     * @return The token name/type.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token token = (Token) obj;
        return id == token.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Token{id=" + id + ", name='" + name + "'}";
    }
}