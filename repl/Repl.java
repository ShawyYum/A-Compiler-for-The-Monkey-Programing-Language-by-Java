package repl;

import compiler.Compiler;
import compiler.SymbolTable;
import lexer.Lexer;
import object.Builtins;
import object.Object;
import parser.Parser;
import vm.VM;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Repl {
    public static void Start() {
        Scanner scanner = new Scanner(System.in);

        System.out.print(">> ");

        StringBuilder line = new StringBuilder();
        while(scanner.hasNextLine()) {
            line.append(scanner.nextLine()).append("\n");
        }

        var text = line.toString();
        var l = new Lexer(text);
        var p = new Parser(l);

        var program = p.ParseProgram();
        if (!p.Errors().isEmpty()) {
            printParserErrors(p.Errors());
            return;
        }

        var constants = new ArrayList<Object>();
        var globals = new ArrayList<Object>(VM.GlobalSize);

        var symbolTable = new SymbolTable();
        for(int i = 0;i < Builtins.builtins.size();i++) {
            symbolTable.DefineBuiltin(i,Builtins.builtins.get(i).Name);
        }

        var comp = new Compiler(symbolTable,constants);
        var err = comp.Compile(program);
        if(!Objects.equals(err.Message, "")) {
            System.out.printf("Woops! Compilation failed:\n %s\n",err.Message);
            return;
        }

        var code = comp.bytecode();

        var machine = new VM(code,globals);
        err = machine.Run();
        if(!Objects.equals(err.Message, "")) {
            System.out.printf("Woops! Executing bytecode failed:\n %s\n",err.Message);
        }

    }

    public static void printParserErrors(ArrayList<String> errors) {
        System.out.println("Whoops! We ran into some Monkey business here!");
        System.out.println(" parser errors:");
        for(var s : errors) {
            System.out.println("\t" + s);
        }
    }
}
