package compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import static compiler.SymbolTable.SymbolScope.*;

public class SymbolTable {
    public enum SymbolScope {
        LOCAL("LOCAL"),
        GLOBAL("GLOBAL"),
        BUILTIN("BUILTIN"),
        FREE("FREE"),
        FUNCTION("FUNCTION");

        private final String v;

        SymbolScope(String v) {
            this.v = v;
        }

        public String getValue() {
            return v;
        }

    }

    public static class Symbol {
        public String Name;
        public String Scope;
        public int Index;

        public Symbol() {}

        public Symbol(String n,int i) {
            Name = n;
            Index = i;
        }

        public Symbol(String n,SymbolScope s,int i) {
            this(n,i);
            Scope = s.getValue();
        }
    }

    public SymbolTable Outer;
    public HashMap<String,Symbol> store;
    public int numDefinitions;
    public ArrayList<Symbol> FreeSymbols;

    public SymbolTable() {
        store = new HashMap<>();
        FreeSymbols = new ArrayList<>();
    }

    public SymbolTable(SymbolTable outer) {
        this();
        Outer = outer;
    }

    public Symbol Define(String name) {
        var symbol = new Symbol(name, numDefinitions);
        symbol.Scope = (Outer == null) ? SymbolScope.GLOBAL.getValue() : SymbolScope.LOCAL.getValue();
        store.put(name,symbol);
        numDefinitions++;
        return symbol;
    }

    public Tuple<Symbol,Boolean> Resolve(String name) {
        var result = new Tuple<>(new Symbol(), false);
        if(store.containsKey(name)) {
            result.first = store.get(name);
            result.second = true;
        }
        else if(Outer != null) {
            result = Outer.Resolve(name);
            if(!result.second) {
                return result;
            }
            if(Objects.equals(result.first.Scope, GLOBAL.getValue()) || Objects.equals(result.first.Scope, BUILTIN.getValue())) {
                return result;
            }
            result.first = defineFree(result.first);
            return result;
        }
        return result;
    }

    public void DefineBuiltin(int index,String name) {
        Symbol symbol = new Symbol(name, BUILTIN, index);
        store.put(name,symbol);
    }

    public void DefineFunctionName(String name) {
        Symbol symbol = new Symbol(name, SymbolScope.FUNCTION, 0);
        store.put(name,symbol);
    }

    public Symbol defineFree(Symbol original) {
        FreeSymbols.add(original);
        var symbol = new Symbol(original.Name, SymbolScope.FREE, FreeSymbols.size() - 1);
        store.put(original.Name,symbol);
        return symbol;
    }

    public static class Tuple<A, B> {
        public A first;
        public B second;

        public Tuple(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
}
