package object;

import ast.Ast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Object {
    public static final java.lang.String NULL_OBJ = "NULL";
    public static final java.lang.String ERROR_OBJ = "ERROR";

    public static final java.lang.String INTEGER_OBJ = "INTEGER";
    public static final java.lang.String BOOLEAN_OBJ = "BOOLEAN";
    public static final java.lang.String CHAR_OBJ = "CHAR";
    public static final java.lang.String STRING_OBJ = "STRING";

    public static final java.lang.String RETURN_VALUE_OBJ = "RETURN_VALUE";

    public static final java.lang.String FUNCTION_OBJ = "FUNCTION";
    public static final java.lang.String BUILTIN_OBJ = "BUILTIN";

    public static final java.lang.String ARRAY_OBJ = "ARRAY";
    public static final java.lang.String HASH_OBJ = "HASH";

    public static final java.lang.String TUPLE_OBJ = "TUPLE";

    public interface BuiltinFunction {
        Object.object Fn(object... args);
    }

    public interface Hashable {
        HashKey Hashkey();
    }

    public interface object {
        java.lang.String Type();
        java.lang.String Inspect();
    }

    public static class HashKey {
        public java.lang.String Type;
        public int Value;

        public HashKey(java.lang.String s, int v) {
            Type = s;
            Value = v;
        }
    }

    public static class Integer implements object,Hashable {
        public int Value;

        public Integer(int v) {
            Value = v;
        }

        public java.lang.String Type() {
            return INTEGER_OBJ;
        }

        public java.lang.String Inspect() {
            return java.lang.String.format("%d",Value);
        }

        public HashKey Hashkey() {
            return new HashKey(Type(),Value);
        }
    }

    public static class Boolean implements object,Hashable {
        public boolean Value;

        public Boolean(boolean b) {
            Value = b;
        }

        public java.lang.String Type() {
            return BOOLEAN_OBJ;
        }

        public java.lang.String Inspect() {
            return java.lang.String.format("%b",Value);
        }

        public HashKey Hashkey() {
            int value;

            if(Value) {
                value = 1;
            }
            else {
                value = 0;
            }

            return new HashKey(Type(),value);
        }
    }

    public static class Null implements object {
        public Null() {

        }

        public java.lang.String Type() {
            return NULL_OBJ;
        }

        public java.lang.String Inspect() {
            return "null";
        }
    }

    public static class ReturnValue implements object {
        public object Value;

        public ReturnValue(object v) {
            Value = v;
        }

        public java.lang.String Type() {
            return RETURN_VALUE_OBJ;
        }

        public java.lang.String Inspect() {
            return Value.Inspect();
        }
    }

    public static class Error implements object {
        public java.lang.String Message;

        public Error(java.lang.String m) {
            Message = m;
        }

        public java.lang.String Type() {
            return ERROR_OBJ;
        }

        public java.lang.String Inspect() {
            return "ERROR: " + Message;
        }
    }

    public static class Function implements object {
        public ArrayList<Ast.Identifier> Parameters;
        public Ast.BlockStatement Body;
        public Environment.environment Env;

        public Function(ArrayList<Ast.Identifier> p, Environment.environment e, Ast.BlockStatement b) {
            Parameters = p;
            Env = e;
            Body = b;
        }

        public java.lang.String Type() {
            return FUNCTION_OBJ;
        }

        public java.lang.String Inspect() {
            StringBuilder output = new StringBuilder("fn(");

            if (!Parameters.isEmpty()) {
                ArrayList<java.lang.String> params = new ArrayList<>();
                for (var s : Parameters) {
                    params.add(s.String());
                }
                output.append(java.lang.String.join(", ", params));
            }

            output.append(") {\n\n");
            return output.toString();
        }
    }

    public static class String implements object,Hashable {
        public java.lang.String Value;

        public String(java.lang.String v) {
            Value = v;
        }

        public java.lang.String Type() {
            return STRING_OBJ;
        }

        public java.lang.String Inspect() {
            return Value;
        }

        public HashKey Hashkey() {
            return new HashKey(Type(),Value.hashCode());
        }
    }

    public static class Builtin implements object {
        public BuiltinFunction Fn;

        public Builtin(BuiltinFunction b) {
            Fn = b;
        }

        public java.lang.String Type() {
            return BUILTIN_OBJ;
        }

        public java.lang.String Inspect() {
            return "builtin function";
        }
    }

    public static class Array implements object {
        public ArrayList<object> Elements;

        public Array(ArrayList<object> e) {
            Elements = e;
        }

        public java.lang.String Type() {
            return ARRAY_OBJ;
        }

        public java.lang.String Inspect() {
            StringBuilder output = new StringBuilder("[");
            ArrayList<java.lang.String> elements = new ArrayList<>();

            for (var s : Elements) {
                elements.add(s.Inspect());
            }

            output.append(java.lang.String.join(", ", elements));
            output.append("]");

            return output.toString();
        }
    }

    public static class HashPair {
        public object Key;
        public object Value;

        public HashPair(object k,object v) {
            Key = k;
            Value = v;
        }
    }

    public static class Hash implements object {
        public HashMap<HashKey,HashPair> Pairs;

        public Hash(HashMap<HashKey,HashPair> p) {
            Pairs = p;
        }

        public java.lang.String Type() {
            return HASH_OBJ;
        }

        public java.lang.String Inspect() {
            List<java.lang.String> pairs = Pairs.values().stream()
                    .map(hashPair -> java.lang.String.format("%s: %s", hashPair.Key.Inspect(), hashPair.Value.Inspect()))
                    .collect(Collectors.toList());

            return "{" + java.lang.String.join(", ", pairs) + "}";
        }
    }

    public static class Tuple implements object {
        public Object.object Object;
        public boolean Bool;

        public Tuple(Object.object o,boolean b) {
            this.Object = o;
            this.Bool = b;
        }

        public java.lang.String Type() {
            return TUPLE_OBJ;
        }

        public java.lang.String Inspect() {
            return "tuple object";
        }
    }

    public static class Char implements object {
        public char Value;

        public Char(char v) {
            Value = v;
        }

        public java.lang.String Type() {
            return CHAR_OBJ;
        }

        public java.lang.String Inspect() {
            return java.lang.String.valueOf(Value);
        }
    }
}
