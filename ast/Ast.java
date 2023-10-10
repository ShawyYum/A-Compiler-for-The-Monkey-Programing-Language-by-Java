package ast;

import token.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Ast {
    public interface Node {
        String TokenLiteral();

        String String();
    }

    public interface Statement extends Node {}

    public interface Expression extends Node {}

    public static class Program implements Node {
        public ArrayList<Statement> Statements = new ArrayList<>();

        @Override
        public String TokenLiteral() {
            if (!Statements.isEmpty()) {
                return Statements.get(0).TokenLiteral();
            } else {
                return "";
            }
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            for(var s : Statements) {
                output.append(s.String());
            }

            return output.toString();
        }
    }

    public static class LetStatement implements Statement {
        public Token Token;
        public Identifier Name;
        public Expression Value;

        public LetStatement(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            output.append(String.format("%s %s =", TokenLiteral(), Name.String()));

            if (Value != null) {
                output.append(String.format(" %s", Value.String()));
            }

            output.append(";");

            return output.toString();
        }
    }

    public static class ReturnStatement implements Statement {
        public Token Token;
        public Expression ReturnValue;

        public ReturnStatement(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            java.lang.StringBuilder output = new StringBuilder();

            output.append(TokenLiteral()).append(" ");

            if(ReturnValue != null) {
                output.append(ReturnValue.String());
            }

            output.append(";");

            return output.toString();
        }
    }

    public static class ExpressionStatement implements Statement {
        public Token Token;
        public Expression Expression;

        public ExpressionStatement(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            if (Expression != null) {
                return Expression.String();
            }
            return "";
        }
    }

    public static class BlockStatement implements Statement {
        public Token Token;
        public ArrayList<Statement> Statements = new ArrayList<>();

        public BlockStatement(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            for (var s : Statements) {
                output.append(s.String());
            }

            return output.toString();
        }
    }

    public static class Identifier implements Expression {
        public Token Token;
        public String Value;

        public Identifier(Token t,String s) {
            Token = t;
            Value = s;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            return Token.Literal;
        }
    }

    public static class Boolean implements Expression {
        public Token Token;
        public java.lang.Boolean Value;

        public Boolean(Token t, boolean b) {
            Token = t;
            Value = b;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            return Token.Literal;
        }
    }

    public static class IntegerLiteral implements Expression {
        public Token Token;
        public int Value;

        public IntegerLiteral(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            return Token.Literal;
        }
    }

    public static class PrefixExpression implements Expression {
        public Token Token;
        public String Operator;
        public Expression Right;

        public PrefixExpression(Token t,String s) {
            Token = t;
            Operator = s;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            output.append("(")
                    .append(Operator)
                    .append(Right.String())
                    .append(")");

            return output.toString();
        }
    }

    public static class InfixExpression implements Expression {
        public Token Token;
        public Expression Left;
        public String Operator;
        public Expression Right;

        public InfixExpression(Token t,String s,Expression e) {
            Token = t;
            Operator = s;
            Left = e;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            output.append("(")
                    .append(Left.String())
                    .append(Operator)
                    .append(Right.String())
                    .append(")");

            return output.toString();
        }
    }

    public static class IfExpression implements Expression {
        public Token Token;
        public Expression Condition;
        public BlockStatement Consequence;
        public BlockStatement Alternative;

        public IfExpression(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            output.append("if ")
                    .append(Condition.String())
                    .append(" ")
                    .append(Consequence.String());

            if (Alternative != null) {
                output.append("else ")
                        .append(Alternative.String());
            }

            return output.toString();
        }
    }

    public static class FunctionLiteral implements Expression {
        public Token Token;
        public ArrayList<Identifier> Parameters = new ArrayList<>();
        public BlockStatement Body;
        public String Name;

        public FunctionLiteral(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            ArrayList<String> params = new ArrayList<>();
            for (var s : Parameters) {
                params.add(s.String());
            }

            output.append(String.format("%s(", TokenLiteral()))
                    .append(String.join(", ", params))
                    .append(") ")
                    .append(Body.String());

            return output.toString();
        }
    }

    public static class CallExpression implements Expression {
        public Token Token;
        public Expression Function;
        public ArrayList<Expression> Arguments = new ArrayList<>();

        public CallExpression(Token t,Expression f) {
            Token = t;
            Function = f;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            ArrayList<String> args = new ArrayList<>();
            for (var s : Arguments) {
                args.add(s.String());
            }

            output.append(String.format("%s(", Function.String()))
                    .append(String.join(", ", args))
                    .append(")");

            return output.toString();
        }
    }

    public static class StringLiteral implements Expression {
        public Token Token;
        public String Value;

        public StringLiteral(Token t,String s) {
            Token = t;
            Value = s;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            return Token.Literal;
        }
    }

    public static class ArrayLiteral implements Expression {
        public Token Token;
        public ArrayList<Expression> ELements = new ArrayList<>();

        public ArrayLiteral(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            ArrayList<String> elements = new ArrayList<>();
            for (var s : ELements) {
                elements.add(s.String());
            }

            output.append("[")
                    .append(String.join(", ", elements))
                    .append("]");

            return output.toString();
        }
    }

    public static class IndexExpression implements Expression {
        public Token Token;
        public Expression Left;
        public Expression Index;

        public IndexExpression(Token t,Expression e) {
            Token = t;
            Left = e;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            output.append("(")
                    .append(Left.String())
                    .append("[")
                    .append(Index.String())
                    .append(")]");

            return output.toString();
        }
    }

    public static class HashLiteral implements Expression {
        public Token Token;
        public HashMap<Expression,Expression> Pairs = new HashMap<>();

        public HashLiteral(Token t) {
            Token = t;
        }

        @Override
        public String TokenLiteral() {
            return Token.Literal;
        }

        @Override
        public String String() {
            StringBuilder output = new StringBuilder();

            ArrayList<String> pairs = new ArrayList<>();
            for (Map.Entry<Expression, Expression> entry : Pairs.entrySet()) {
                pairs.add(entry.getKey().String() + ":" + entry.getValue().String());
            }

            output.append("{")
                    .append(String.join(", ", pairs))
                    .append("}");

            return output.toString();
        }
    }

    public static class CharLiteral implements Expression {
        public Token Token;
        public char Value;

        public CharLiteral(Token t) {
            Token = t;
        }

        public String TokenLiteral() {
            return Token.Literal;
        }

        public String String() {
            return String.valueOf(Value);
        }
    }
}
