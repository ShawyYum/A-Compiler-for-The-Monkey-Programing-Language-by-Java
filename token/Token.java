package token;

import java.util.HashMap;

public class Token {
    public static final String ILLEAGL = "ILLEGAL";
    public static final String EOF = "EOF";

    public static final String IDENT = "IDENT";
    public static final String INT = "INT";
    public static final String CHAR = "CHAR";
    public static final String STRING = "STRING";

    public static final String ASSIGN = "=";
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String BANG = "!";
    public static final String ASTERISK = "*";
    public static final String SLASH = "/";

    public static final String LT = "<";
    public static final String GT = ">";
    public static final String LTASSIGN = "<=";
    public static final String GTASSIGN = ">=";
    public static final String EQ = "==";
    public static final String NOT_EQ = "!=";

    public static final String COMMA = ",";
    public static final String SEMICOLON = ";";
    public static final String COLON = ":";
    public static final String LPAREN = "(";
    public static final String RPAREN = ")";
    public static final String LBRACE = "{";
    public static final String RBRACE = "}";
    public static final String LBRACKET = "[";
    public static final String RBRACKET = "]";

    public static final String FUNCTION = "FUNCTION";
    public static final String LET = "LET";
    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";
    public static final String IF = "IF";
    public static final String ELSE = "ELSE";
    public static final String RETURN = "RETURN";

    public String Type;
    public String Literal;

    public Token(String type,String literal) {
        Type = type;
        Literal = literal;
    }

    private static final HashMap<String, String> keywords = new HashMap<>() {{
        put("fn", FUNCTION);
        put("let", LET);
        put("true", TRUE);
        put("false", FALSE);
        put("if", IF);
        put("else", ELSE);
        put("return", RETURN);
    }};

    public static String LookupIdent(String ident) {
        if (keywords.containsKey(ident)) {
            return keywords.get(ident);
        }
        return IDENT;
    }
}
