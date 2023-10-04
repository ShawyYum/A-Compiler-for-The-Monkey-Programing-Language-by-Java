package evaluator;

import ast.Ast;
import object.Environment;
import object.Object;
import java.util.*;
import java.util.stream.IntStream;

public class Evaluator {
    public static Object.object Eval(Ast.Node node, Environment.environment env) {
        switch (node) {
            case Ast.Program program -> {
                return evalProgram(program, env);
            }
            case Ast.BlockStatement blockStatement -> {
                return evalBlockStatement(blockStatement, env);
            }
            case Ast.ExpressionStatement expressionStatement -> {
                return Eval(expressionStatement.Expression, env);
            }
            case Ast.ReturnStatement returnStatement -> {
                var val = Eval(returnStatement.ReturnValue, env);
                if (isNull(val) || isError(val)) {
                    return val;
                }
                return new Object.ReturnValue(val);
            }
            case Ast.LetStatement letStatement -> {
                var val = Eval(letStatement.Value, env);
                if (isNull(val) || isError(val)) {
                    return val;
                }
                return env.Set(letStatement.Name.Value, val);
            }
            case Ast.IntegerLiteral integerLiteral -> {
                return new Object.Integer(integerLiteral.Value);
            }
            case Ast.CharLiteral charLiteral -> {
                return new Object.Char(charLiteral.Value);
            }
            case Ast.StringLiteral stringLiteral -> {
                return new Object.String(stringLiteral.Value);
            }
            case Ast.Boolean aBoolean -> {
                return nativeBoolToBooleanObject(aBoolean.Value);
            }
            case Ast.PrefixExpression prefixExpression -> {
                var right = Eval(prefixExpression.Right, env);
                if (isNull(right) || isError(right)) {
                    return right;
                }
                return evalPrefixExpression(prefixExpression.Operator, right);
            }
            case Ast.InfixExpression infixExpression -> {
                var left = Eval(infixExpression.Left, env);
                if (isNull(left) || isError(left)) {
                    return left;
                }

                var right = Eval(infixExpression.Right, env);
                if (isNull(right) || isError(right)) {
                    return right;
                }

                return evalInfixExpression(infixExpression.Operator, left, right);
            }
            case Ast.IfExpression ifExpression -> {
                return evalIfExpression(ifExpression, env);
            }
            case Ast.Identifier identifier -> {
                return evalIdentifier(identifier, env);
            }
            case Ast.FunctionLiteral functionLiteral -> {
                var params = functionLiteral.Parameters;
                var body = functionLiteral.Body;
                return new Object.Function(params, env, body);
            }
            case Ast.CallExpression callExpression -> {
                var function = Eval(callExpression.Function, env);
                if (isNull(function) || isError(function)) {
                    return function;
                }
                var args = evalExpressions(callExpression.Arguments, env);
                if (args.size() == 1 && isError(args.get(0))) {
                    return args.get(0);
                }
                return applyFunction(function, args);
            }
            case Ast.ArrayLiteral arrayLiteral -> {
                var elements = evalExpressions(arrayLiteral.ELements, env);
                if (elements.size() == 1 && isError(elements.get(0))) {
                    return elements.get(0);
                }
                return new Object.Array(elements);
            }
            case Ast.IndexExpression indexExpression -> {
                var left = Eval(indexExpression.Left, env);
                if (isNull(left) || isError(left)) {
                    return left;
                }
                var index = Eval(indexExpression.Index, env);
                if (isNull(index) || isError(index)) {
                    return index;
                }
                return evalIndexExpression(left, index);
            }
            case Ast.HashLiteral hashLiteral -> {
                return evalHashLiteral(hashLiteral, env);
            }
            case null, default -> {
                return new Object.Null();
            }
        }
    }

    public static Object.object evalProgram(Ast.Program program, Environment.environment env) {
        Object.object result = null;

        for(var s : program.Statements) {
            result = Eval(s,env);

            var rt = result.Type();
            switch (rt) {
                case Object.RETURN_VALUE_OBJ:
                    return ((Object.ReturnValue)result).Value;
                case Object.ERROR_OBJ, Object.NULL_OBJ:
                    return result;
                default:
            }
        }

        return result;
    }

    public static Object.object evalBlockStatement(Ast.BlockStatement block,Environment.environment env) {
        Object.object result = null;

        for(var s : block.Statements) {
            result = Eval(s,env);

            var rt = result.Type();
            if(Objects.equals(rt,Object.RETURN_VALUE_OBJ) || Objects.equals(rt,Object.ERROR_OBJ) || Objects.equals(rt,Object.NULL_OBJ)) {
                return result;
            }
        }

        return result;
    }

    public static Object.Boolean nativeBoolToBooleanObject(boolean input) {
        if(input) {
            return new Object.Boolean(true);
        }
        else {
            return new Object.Boolean(false);
        }
    }

    public static Object.object evalPrefixExpression(String operator,Object.object right) {
        return switch (operator) {
            case "!" -> evalBangOperatorExpression(right);
            case "-" -> evalMinusPrefixOperatorExpression(right);
            default -> new Object.Error(String.format("unknown operator: %s%s", operator, right.Type()));
        };
    }

    public static Object.object evalInfixExpression(String operator,Object.object left,Object.object right) {
        if(Objects.equals(left.Type(),right.Type())) {
            if(Objects.equals(left.Type(), Object.INTEGER_OBJ)) {
                return evalIntegerInfixExpression(operator,left,right);
            }
            else if(Objects.equals(left.Type(), Object.CHAR_OBJ)) {
                return evalCharInfixExpression(operator,left,right);
            }
            else if(Objects.equals(left.Type(), Object.STRING_OBJ)) {
                return evalStringInfixExpression(operator,left,right);
            }
            else {
                return new Object.Error(String.format("type cannot operator: %s %s %s",
                        left.Type(),operator,right.Type()));
            }
        }
        else {
            return new Object.Error(String.format("type mismatch: %s %s %s",
                    left.Type(),operator,right.Type()));
        }
    }

    public static Object.object evalBangOperatorExpression(Object.object right) {
        if(Objects.equals(right,new Object.Boolean(true))) {
            return new Object.Boolean(false);
        }
        else if(Objects.equals(right,new Object.Boolean(false))) {
            return new Object.Boolean(true);
        }
        else if(Objects.equals(right,new Object.Null())) {
            return new Object.Boolean(true);
        }
        else {
            return new Object.Boolean(false);
        }
    }

    public static Object.object evalMinusPrefixOperatorExpression(Object.object right) {
        if(!Objects.equals(right.Type(), Object.INTEGER_OBJ)) {
            return new Object.Error(String.format("unknown operator: -%s",right.Type()));
        }
        else {
            var value = ((Object.Integer)right).Value;
            return new Object.Integer(-value);
        }
    }

    public static Object.object evalIntegerInfixExpression(String operator,Object.object left,Object.object right) {
        var leftVal = ((Object.Integer)left).Value;
        var rightVal = ((Object.Integer)right).Value;

        return switch (operator) {
            case "+" -> new Object.Integer(leftVal + rightVal);
            case "-" -> new Object.Integer(leftVal - rightVal);
            case "*" -> new Object.Integer(leftVal * rightVal);
            case "/" -> {
                if (rightVal == 0) {
                    yield new Object.Error(String.format("divisor: %s is 0", right.Inspect()));
                }
                yield new Object.Integer(leftVal / rightVal);
            }
            case "<" -> new Object.Integer(leftVal < rightVal ? 1 : 0);
            case ">" -> new Object.Integer(leftVal > rightVal ? 1 : 0);
            case "==" -> new Object.Integer(leftVal == rightVal ? 1 : 0);
            case "!=" -> new Object.Integer(leftVal != rightVal ? 1 : 0);
            case "<=" -> new Object.Integer(leftVal <= rightVal ? 1 : 0);
            case ">=" -> new Object.Integer(leftVal >= rightVal ? 1 : 0);
            default -> new Object.Error(String.format("unknown operator: %s %s %s",
                    left.Type(), operator, right.Type()));
        };
    }

    public static Object.object evalCharInfixExpression(String operator,Object.object left,Object.object right) {
        var leftVal = ((Object.Char)left).Value;
        var rightVal = ((Object.Char)right).Value;

        return switch (operator) {
            case "==" -> new Object.Integer(leftVal == rightVal ? 1 : 0);
            case "!=" -> new Object.Integer(leftVal != rightVal ? 1 : 0);
            default -> new Object.Error(String.format("unknown operator: %s %s %s",
                    left.Type(), operator, right.Type()));
        };
    }

    public static Object.object evalStringInfixExpression(String operator,Object.object left,Object.object right) {
        var leftVal = ((Object.String)left).Value;
        var rightVal = ((Object.String)right).Value;

        return switch (operator) {
            case "+" -> new Object.Error(String.format("unknown operator: %s %s %s",
                    left.Type(),operator,right.Type()));
            default -> new Object.String(leftVal + rightVal);
        };
    }

    public static Object.object evalIfExpression(Ast.IfExpression ie,Environment.environment env) {
        var condition = Eval(ie.Condition,env);
        if(isNull(condition) || isError(condition)) {
            return condition;
        }
        else if(isTruthy(condition)) {
            return Eval(ie.Consequence,env);
        }
        else if(!Objects.equals(ie.Alternative,null)) {
            return Eval(ie.Alternative,env);
        }
        else {
            return new Object.Null();
        }
    }

    public static Object.object evalIdentifier(Ast.Identifier node,Environment.environment env) {
        if(env.Get(node.Value).Bool) {
            return env.Get(node.Value).Object;
        }
        else if(Builtins.builtins.containsKey(node.Value)) {
            return Builtins.builtins.get(node.Value);
        }
        else {
            return new Object.Error(String.format("identifier not found: " + node.Value));
        }
    }

    public static boolean isTruthy(Object.object obj) {
        if(isNull(obj)) {
            return false;
        }
        else if(Objects.equals(obj,new Object.Boolean(true))) {
            return true;
        }
        else {
            return !Objects.equals(obj,new Object.Boolean(false));
        }
    }

    public static boolean isError(Object.object obj) {
        if(!Objects.equals(obj,new Object.Null())) {
            return Objects.equals(obj.Type(),Object.ERROR_OBJ);
        }
        return false;
    }

    public static ArrayList<Object.object> evalExpressions(ArrayList<Ast.Expression> exps,Environment.environment env) {
        var result = new ArrayList<Object.object>();

        for(var s : exps) {
            var evaluated = Eval(s,env);
            if(isNull(evaluated) || isError(evaluated)) {
                return new ArrayList<>(Collections.singletonList(evaluated));
            }
            result.add(evaluated);
        }

        return result;
    }

    public static Object.object applyFunction(Object.object fn,ArrayList<Object.object> args) {
        switch (fn) {
            case Object.Function function -> {
                var extendedEnv = extendFunctionEnv(function,args);
                var evaluated = Eval(function.Body,extendedEnv);
                return unwrapReturnValue(evaluated);
            }
            case Object.Builtin builtin -> {
                return builtin.Fn.Fn(args.toArray(new Object.object[0]));
            }
            default -> {
                return new Object.Error(String.format("not a function: %s",fn.Type()));
            }
        }
    }

    public static Environment.environment extendFunctionEnv(Object.Function fn, ArrayList<Object.object> args) {
        var env = new Environment.environment().NewEnclosedEnvironment(fn.Env);

        IntStream.range(0, fn.Parameters.size())
                .forEach(i -> env.Set(fn.Parameters.get(i).Value, args.get(i)));

        return env;
    }


    public static Object.object unwrapReturnValue(Object.object obj) {
        return switch (obj) {
            case Object.ReturnValue returnValue -> returnValue.Value;
            default -> obj;
        };
    }

    public static Object.object evalIndexExpression(Object.object left,Object.object index) {
        if(Objects.equals(left.Type(),Object.STRING_OBJ) && Objects.equals(index.Type(),Object.INTEGER_OBJ)) {
            return evalStringIndexExpression(left,index);
        }
        else if(Objects.equals(left.Type(), Object.ARRAY_OBJ) && Objects.equals(index.Type(), Object.INTEGER_OBJ)) {
            return evalArrayIndexExpression(left,index);
        }
        else if(Objects.equals(left.Type(), Object.HASH_OBJ)) {
            return evalHashIndexExpression(left,index);
        }
        else {
            return new Object.Error(String.format("index operator not supported: %s",left.Type()));
        }
    }

    public static Object.object evalStringIndexExpression(Object.object string,Object.object index) {
        var stringObject = (Object.String)string;
        var idx = ((Object.Integer)index).Value;
        var max = stringObject.Value.length() - 1;

        if(idx < 0 || idx > max) {
            return new Object.Error(String.format("index out of bounds: %d",idx));
        }
        else {
            return new Object.Char(stringObject.Value.charAt(idx));
        }
    }
    public static Object.object evalArrayIndexExpression(Object.object array,Object.object index) {
        var arrayObject = (Object.Array)array;
        var idx = ((Object.Integer)index).Value;
        var max = arrayObject.Elements.size() - 1;

        if(idx < 0 || idx > max) {
            return new Object.Error(String.format("index out of bounds: %d",idx));
        }
        else {
            return arrayObject.Elements.get(idx);
        }
    }

    public static Object.object evalHashLiteral(Ast.HashLiteral node,Environment.environment env) {
        var pairs = new HashMap<Object.HashKey,Object.HashPair>();

        for(Map.Entry<Ast.Expression, Ast.Expression> s : node.Pairs.entrySet()) {
            Ast.Expression keyExpression = s.getKey();
            var key = Eval(keyExpression,env);
            if(isNull(key) || isError(key)) {
                return key;
            }

            if(!(key instanceof Object.Hashable hashKey)) {
                return new Object.Error(String.format("unusable as hash key: %s",key.Type()));
            }

            Ast.Expression valueExpression = s.getValue();
            var value = Eval(valueExpression,env);
            if(isNull(value) || isError(value)) {
                return value;
            }

            Object.HashKey hashed = hashKey.Hashkey();
            pairs.put(hashed,new Object.HashPair(key,value));
        }

        return new Object.Hash(pairs);
    }

    public static Object.object evalHashIndexExpression(Object.object hash,Object.object index) {
        var hashObject = (Object.Hash)hash;

        if(!(index instanceof Object.Hashable key)) {
            return new Object.Error(String.format("unusable as hash key: %s",index.Type()));
        }

        if(!hashObject.Pairs.containsKey(key.Hashkey())) {
            return new Object.Null();
        }

        var pair = hashObject.Pairs.get(key.Hashkey());

        return pair.Value;
    }

    public static boolean isNull(Object.object obj) {
        return Objects.equals(obj,null) || Objects.equals(obj.Type(),Object.NULL_OBJ);
    }
}
