package org.openl.rules.fuzzy;

public class Token {

    private final String value;
    private final int distance;
    private final int minMatchedTokens;

    public Token(String value, int distance) {
        this(value, distance, 0);
    }

    public Token(String value, int distance, int minMatchedTokens) {
        this.value = value;
        this.distance = distance;
        this.minMatchedTokens = minMatchedTokens;
    }

    public String getValue() {
        return value;
    }

    public int getDistance() {
        return distance;
    }

    public int getMinMatchedTokens() {
        return minMatchedTokens;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + distance;
        result = prime * result + (value == null ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Token other = (Token) obj;
        if (distance != other.distance) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Token [value=" + value + "]";
    }

}
