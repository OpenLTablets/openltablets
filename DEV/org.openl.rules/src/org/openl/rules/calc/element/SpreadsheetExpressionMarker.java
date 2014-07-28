package org.openl.rules.calc.element;

import org.apache.commons.lang3.StringUtils;

public enum SpreadsheetExpressionMarker {

    OPEN_CURLY_BRACKET("{"),
    CLOSED_CURLY_BRACKET("}"),
    EQUALS_SIGN("=");

    private String symbol;

    private SpreadsheetExpressionMarker(String marker) {
        this.symbol = marker;
    }

    public static boolean isFormula(String src) {

        if (StringUtils.isBlank(src)) {
            return false;
        }

        if (src.startsWith(OPEN_CURLY_BRACKET.getSymbol())
                && src.endsWith(CLOSED_CURLY_BRACKET.getSymbol())) {
            return true;
        }

        if (src.startsWith(EQUALS_SIGN.getSymbol())
                && (src.length() > 2 || src.length() == 2 && Character.isLetterOrDigit(src.charAt(1)))) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return name() + symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
