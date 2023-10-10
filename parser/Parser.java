package parser;

import ast.Ast;
import lexer.Lexer;
import token.Token;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Parser {
    public enum Precedence {
        __(0),
        LOWEST(1),
        EQUALS(2),
        LESS(3),
        GREATER(4),
        SUM(5),
        PRODUCT(6),
        PREFIX(7),
        CALL(8),
        INDEX(9);

        private final int Value;

        Precedence(int v) {
            Value = v;
        }

        public int getValue() {
            return Value;
        }
    }

    public static final HashMap<String,Integer> precedences = new HashMap<>() {{
        put(Token.EQ, Precedence.EQUALS.getValue());
        put(Token.NOT_EQ, Precedence.EQUALS.getValue());
        put(Token.LT, Precedence.LESS.getValue());
        put(Token.LTASSIGN,Precedence.LESS.getValue());
        put(Token.GT, Precedence.GREATER.getValue());
        put(Token.GTASSIGN,Precedence.GREATER.getValue());
        put(Token.PLUS, Precedence.SUM.getValue());
        put(Token.MINUS, Precedence.SUM.getValue());
        put(Token.SLASH, Precedence.PRODUCT.getValue());
        put(Token.ASTERISK, Precedence.PRODUCT.getValue());
        put(Token.LPAREN, Precedence.CALL.getValue());
        put(Token.LBRACKET, Precedence.INDEX.getValue());
    }};

    public interface prefixParseFn {
        Ast.Expression prefixParseFn();
    }

    public interface infixParseFn {
        Ast.Expression infixParseFn(Ast.Expression expression);
    }

    public Lexer l;
    public ArrayList<String> errors = new ArrayList<>();
    public Token curToken;
    public Token peekToken;
    public HashMap<String,prefixParseFn> prefixParseFns = new HashMap<>();
    public HashMap<String,infixParseFn> infixParseFns = new HashMap<>();

    public Parser(Lexer l) {
        this.l = l;

        prefixParseFns.put(Token.IDENT, this::parseIdentifier);
        prefixParseFns.put(Token.INT, this::parseIntegerLiteral);
        prefixParseFns.put(Token.CHAR,this::parseCharLiteral);
        prefixParseFns.put(Token.STRING, this::parseStringLiteral);
        prefixParseFns.put(Token.BANG, this::parsePrefixExpression);
        prefixParseFns.put(Token.MINUS, this::parsePrefixExpression);
        prefixParseFns.put(Token.TRUE, this::parseBoolean);
        prefixParseFns.put(Token.FALSE, this::parseBoolean);
        prefixParseFns.put(Token.LPAREN, this::parseGroupedExpression);
        prefixParseFns.put(Token.IF, this::parseIfExpression);
        prefixParseFns.put(Token.FUNCTION, this::parseFunctionLiteral);
        prefixParseFns.put(Token.LBRACKET, this::parseArrayLiteral);
        prefixParseFns.put(Token.LBRACE, this::parseHashLiteral);

        infixParseFns.put(Token.PLUS, this::parseInfixExpression);
        infixParseFns.put(Token.MINUS, this::parseInfixExpression);
        infixParseFns.put(Token.SLASH, this::parseInfixExpression);
        infixParseFns.put(Token.ASTERISK, this::parseInfixExpression);
        infixParseFns.put(Token.EQ, this::parseInfixExpression);
        infixParseFns.put(Token.NOT_EQ, this::parseInfixExpression);
        infixParseFns.put(Token.LT, this::parseInfixExpression);
        infixParseFns.put(Token.LTASSIGN,this::parseInfixExpression);
        infixParseFns.put(Token.GT, this::parseInfixExpression);
        infixParseFns.put(Token.GTASSIGN,this::parseInfixExpression);

        infixParseFns.put(Token.LPAREN, this::parseCallExpression);
        infixParseFns.put(Token.LBRACKET, this::parseIndexExpression);

        nextToken();
        nextToken();
    }

    public void nextToken() {
        curToken = peekToken;
        peekToken = l.NextToken();
    }

    public boolean curTokenIs(String t) {
        return Objects.equals(curToken.Type,t);
    }

    public boolean peekTokenIs(String t) {
        return Objects.equals(peekToken.Type,t);
    }

    public boolean expectPeek(String t) {
        if(peekTokenIs(t)) {
            nextToken();
            return true;
        }
        else {
            peekError(t);
            return false;
        }
    }

    public ArrayList<String> Errors() {
        return errors;
    }

    public void peekError(String t) {
        errors.add(String.format("expected next token to be %s, got %s instead",t,peekToken.Type));
    }

    public void noPrefixParseFnError(String t) {
        errors.add(String.format("no prefix parse function for %s found",t));
    }

    public Ast.Program ParseProgram() {
        var program = new Ast.Program();
        program.Statements = new ArrayList<>();

        while(!curTokenIs(Token.EOF)) {
            var stmt = parseStatement();
            if(!Objects.equals(stmt,null)) {
                program.Statements.add(stmt);
            }
            nextToken();
        }

        return program;
    }

    public Ast.Statement parseStatement() {
        return switch (curToken.Type) {
            case Token.LET -> parseLetStatement();
            case Token.RETURN -> parseReturnStatement();
            default -> parseExpressionstatement();
        };
    }

    public Ast.LetStatement parseLetStatement() {
        var stmt = new Ast.LetStatement(curToken);

        if(!expectPeek(Token.IDENT)) {
            return null;
        }

        stmt.Name = new Ast.Identifier(curToken,curToken.Literal);

        if(!expectPeek(Token.ASSIGN)) {
            return null;
        }

        nextToken();

        stmt.Value = parseExpression(Precedence.LOWEST.getValue());

        if(stmt.Value instanceof Ast.FunctionLiteral) {
            ((Ast.FunctionLiteral) stmt.Value).Name = stmt.Name.Value;
        }

        if(peekTokenIs(Token.SEMICOLON)) {
            nextToken();
        }

        return stmt;
    }

    public Ast.ReturnStatement parseReturnStatement() {
        var stmt = new Ast.ReturnStatement(curToken);

        nextToken();

        stmt.ReturnValue = parseExpression(Precedence.LOWEST.getValue());

        if(peekTokenIs(Token.SEMICOLON)) {
            nextToken();
        }

        return stmt;
    }

    public Ast.ExpressionStatement parseExpressionstatement() {
        var stmt = new Ast.ExpressionStatement(curToken);

        stmt.Expression = parseExpression(Precedence.LOWEST.getValue());

        if(peekTokenIs(Token.SEMICOLON)) {
            nextToken();
        }

        return stmt;
    }

    public Ast.Expression parseExpression(int precedence) {
        var prefix = prefixParseFns.get(curToken.Type);
        if(Objects.equals(prefix,null)) {
            noPrefixParseFnError(curToken.Type);
            return null;
        }
        var leftExp = prefix.prefixParseFn();

        while(!peekTokenIs(Token.SEMICOLON) && precedence < peekPrecedence()) {
            var infix = infixParseFns.get(peekToken.Type);
            if(Objects.equals(infix,null)) {
                return leftExp;
            }

            nextToken();

            leftExp = infix.infixParseFn(leftExp);
        }

        return leftExp;
    }

    public int peekPrecedence() {
        if(precedences.containsKey(peekToken.Type)) {
            return precedences.get(peekToken.Type);
        }


        return Precedence.LOWEST.getValue();
    }

    public int curPrecedence() {
        if(precedences.containsKey(curToken.Type)) {
            return precedences.get(curToken.Type);
        }

        return Precedence.LOWEST.getValue();
    }

    public Ast.Expression parseIdentifier() {
        return new Ast.Identifier(curToken, curToken.Literal);
    }

    public Ast.Expression parseIntegerLiteral() {
        var lit =  new Ast.IntegerLiteral(curToken);

        int value = 0;
        try {
            value = Integer.parseInt(curToken.Literal);
        } catch (NumberFormatException err) {
            errors.add(String.format("could not parse %s as integer",curToken.Literal));
        }

        lit.Value = value;
        return lit;
    }

    public Ast.Expression parseStringLiteral() {
        return new Ast.StringLiteral(curToken,curToken.Literal);
    }

    public Ast.Expression parsePrefixExpression() {
        var expression = new Ast.PrefixExpression(curToken,curToken.Literal);

        nextToken();

        expression.Right = parseExpression(Precedence.PREFIX.Value);

        return expression;
    }

    public Ast.Expression parseInfixExpression(Ast.Expression left) {
        var expression = new Ast.InfixExpression(curToken,curToken.Literal,left);

        var precedence = curPrecedence();
        nextToken();
        expression.Right = parseExpression(precedence);

        return expression;
    }

    public Ast.Expression parseBoolean() {
        return new Ast.Boolean(curToken,curTokenIs(Token.TRUE));
    }

    public Ast.Expression parseGroupedExpression() {
        nextToken();

        var exp = parseExpression(Precedence.LOWEST.Value);

        if(!expectPeek(Token.RPAREN)) {
            return null;
        }

        return exp;
    }

    public Ast.Expression parseIfExpression() {
        var expression = new Ast.IfExpression(curToken);

        if(!expectPeek(Token.LPAREN)) {
            return null;
        }

        nextToken();
        expression.Condition = parseExpression(Precedence.LOWEST.getValue());

        if(!expectPeek(Token.RPAREN)) {
            return null;
        }

        if(!expectPeek(Token.LBRACE)) {
            return null;
        }

        expression.Consequence = parseBlockStatement();

        if(peekTokenIs(Token.ELSE)) {
            nextToken();

            if(!expectPeek(Token.LBRACE)) {
                return null;
            }

            expression.Alternative = parseBlockStatement();
        }

        return expression;
    }

    public Ast.BlockStatement parseBlockStatement() {
        var block = new Ast.BlockStatement(curToken);
        block.Statements = new ArrayList<>();

        nextToken();

        while(!curTokenIs(Token.RBRACE) && !curTokenIs(Token.EOF)) {
            var stmt = parseStatement();
            if(stmt != null) {
                block.Statements.add(stmt);
            }
            nextToken();
        }

        return block;
    }

    public Ast.Expression parseFunctionLiteral() {
        var lit = new Ast.FunctionLiteral(curToken);

        if(!expectPeek(Token.LPAREN)) {
            return null;
        }

        lit.Parameters = parseFunctionParameters();

        if(!expectPeek(Token.LBRACE)) {
            return null;
        }

        lit.Body = parseBlockStatement();

        return lit;
    }

    public ArrayList<Ast.Identifier> parseFunctionParameters() {
        var identifiers = new ArrayList<Ast.Identifier>();

        if(peekTokenIs(Token.RPAREN)) {
            nextToken();
            return identifiers;
        }

        nextToken();

        var ident = new Ast.Identifier(curToken, curToken.Literal);
        identifiers.add(ident);

        while(peekTokenIs(Token.COMMA)) {
            nextToken();
            nextToken();
            ident = new Ast.Identifier(curToken, curToken.Literal);
            identifiers.add(ident);
        }

        if(!expectPeek(Token.RPAREN)) {
            return null;
        }

        return identifiers;
    }

    public Ast.Expression parseCallExpression(Ast.Expression function) {
        var exp = new Ast.CallExpression(curToken,function);
        exp.Arguments = parseExpressionList(Token.RPAREN);
        return exp;
    }

    public ArrayList<Ast.Expression> parseExpressionList(String end) {
        var list = new ArrayList<Ast.Expression>();

        if(peekTokenIs(end)) {
            nextToken();
            return list;
        }

        nextToken();
        list.add(parseExpression(Precedence.LOWEST.getValue()));

        while(peekTokenIs(Token.COMMA)) {
            nextToken();
            nextToken();
            list.add(parseExpression(Precedence.LOWEST.getValue()));
        }

        if(!expectPeek(end)) {
            return null;
        }

        return list;
    }

    public Ast.Expression parseArrayLiteral() {
        var array = new Ast.ArrayLiteral(curToken);

        array.ELements = parseExpressionList(Token.RBRACKET);

        return array;
    }

    public Ast.Expression parseIndexExpression(Ast.Expression left) {
        var exp = new Ast.IndexExpression(curToken,left);

        nextToken();
        exp.Index = parseExpression(Precedence.LOWEST.getValue());

        if(!expectPeek(Token.RBRACKET)) {
            return null;
        }

        return exp;
    }

    public Ast.Expression parseHashLiteral() {
        var hash = new Ast.HashLiteral(curToken);
        hash.Pairs = new HashMap<>();

        while(!peekTokenIs(Token.RBRACE)) {
            nextToken();
            var key = parseExpression(Precedence.LOWEST.getValue());

            if(!expectPeek(Token.COLON)) {
                return null;
            }

            nextToken();
            var value = parseExpression(Precedence.LOWEST.getValue());

            hash.Pairs.put(key,value);

            if(!peekTokenIs(Token.RBRACE) && !expectPeek(Token.COMMA)) {
                return null;
            }
        }

        if(!expectPeek(Token.RBRACE)) {
            return null;
        }

        return hash;
    }

    public Ast.Expression parseCharLiteral() {
        var lit = new Ast.CharLiteral(curToken);
        char value = 0;

        if (curToken.Literal.length() > 1) {
            errors.add(String.format("could not parse %s as char",curToken.Literal));
        }
        else {
            value = curToken.Literal.charAt(0);
        }

        lit.Value = value;
        return lit;
    }
}

