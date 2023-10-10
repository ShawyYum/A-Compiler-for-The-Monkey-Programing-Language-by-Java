package lexer;

import token.Token;
import java.util.Objects;

public class Lexer {
    private final String input;
    private int curPosition;
    private int nextPosition;
    private char ch;

    public Lexer(String input) {
        this.input = input;
        readNext();
    }

    public Token NextToken() {
        Token tok;

        skipWhitespace();

        switch(ch) {
            case '=':
                if(Objects.equals(peekChar(),'=')) {
                    readNext();
                    tok = new Token(Token.EQ,"==");
                }
                else {
                    tok = newToken(Token.ASSIGN,ch);
                }
                break;
            case '+':
                tok = newToken(Token.PLUS,ch);
                break;
            case '-':
                tok = newToken(Token.MINUS,ch);
                break;
            case '!':
                if(Objects.equals(peekChar(),'=')) {
                    readNext();
                    tok = new Token(Token.NOT_EQ,"!=");
                }
                else {
                    tok = newToken(Token.BANG,ch);
                }
                break;
            case '/':
                tok = newToken(Token.SLASH,ch);
                break;
            case '*':
                tok = newToken(Token.ASTERISK,ch);
                break;
            case '<':
                if(Objects.equals(peekChar(),'=')) {
                    readNext();
                    tok = new Token(Token.LTASSIGN,"<=");
                }
                else {
                    tok = newToken(Token.LT,ch);
                }
                break;
            case '>':
                if(Objects.equals(peekChar(),'=')) {
                    readNext();
                    tok = new Token(Token.GTASSIGN,">=");
                }
                else {
                    tok = newToken(Token.GT,ch);
                }
                break;
            case '{':
                tok = newToken(Token.LBRACE,ch);
                break;
            case '}':
                tok = newToken(Token.RBRACE,ch);
                break;
            case '(':
                tok = newToken(Token.LPAREN,ch);
                break;
            case ')':
                tok = newToken(Token.RPAREN,ch);
                break;
            case '\'':
                tok = new Token(Token.CHAR,readChar());
                break;
            case '"':
                tok = new Token(Token.STRING,readString());
                break;
            case '[':
                tok = newToken(Token.LBRACKET,ch);
                break;
            case ']':
                tok = newToken(Token.RBRACKET,ch);
                break;
            case ';':
                tok = newToken(Token.SEMICOLON,ch);
                break;
            case ':':
                tok = newToken(Token.COLON,ch);
                break;
            case ',':
                tok = newToken(Token.COMMA,ch);
                break;
            case 0:
                tok = new Token(Token.EOF,"");
                break;
            default:
                if(Character.isLetter(ch) || Objects.equals(ch,'_')) {
                    String tmp = readIdentifier();
                    tok = new Token(Token.LookupIdent(tmp),tmp);
                    return tok;
                }
                else if(Character.isDigit(ch)) {
                    tok = new Token(Token.INT,readNumber());
                    return tok;
                }
                else {
                    tok = newToken(Token.ILLEAGL,ch);
                }
                break;
        }

        readNext();
        return tok;
    }

    private void skipWhitespace() {
        while(Objects.equals(ch,' ') || Objects.equals(ch,'\t') || Objects.equals(ch,'\n') || Objects.equals(ch,'\r')) {
            readNext();
        }
    }

    private void readNext() {
        if(nextPosition >= input.length()) {
            ch = 0;
        }
        else {
            ch = input.charAt(nextPosition);
        }
        curPosition = nextPosition;
        nextPosition += 1;
    }

    private char peekChar() {
        if(nextPosition >= input.length()) {
            return 0;
        }
        else {
            return input.charAt(nextPosition);
        }
    }

    private String readIdentifier() {
        int position = curPosition;

        while (Character.isLetter(ch) || Objects.equals(ch,'_')) {
            readNext();
        }

        return input.substring(position, curPosition);
    }

    private String readNumber() {
        int position = curPosition;

        while (Character.isDigit(ch)) {
            readNext();
        }

        return input.substring(position, curPosition);
    }

    private String readChar() {
        int position = curPosition + 1;

        do {
            readNext();
        } while(!Objects.equals(ch,'\'') && !Objects.equals(ch,0));

        return input.substring(position,curPosition);
    }

    private String readString() {
        int position = curPosition + 1;

        do {
            readNext();
        } while (!Objects.equals(ch,'"') && !Objects.equals(ch,0));

        return input.substring(position, curPosition);
    }

    private Token newToken(String tokenType,char ch) {
        return new Token(tokenType,String.valueOf(ch));
    }
}

