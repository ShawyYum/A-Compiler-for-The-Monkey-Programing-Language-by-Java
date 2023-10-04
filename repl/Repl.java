package repl;

import evaluator.Evaluator;
import lexer.Lexer;
import object.Environment;
import parser.Parser;
import java.util.ArrayList;
import java.util.Scanner;

public class Repl {
    public static void Start() {
        Scanner scanner = new Scanner(System.in);
        var env = new Environment.environment();

        System.out.print(">> ");

        StringBuilder line = new StringBuilder();
        while(scanner.hasNextLine()) {
            line.append(scanner.nextLine()).append("\n");
        }

        String text = line.toString();
        var l = new Lexer.lexer(text);
        var p = new Parser.parser(l);

        var program = p.ParseProgram();
        if (!p.Errors().isEmpty()) {
            printParserErrors(p.Errors());
            return;
        }

        Evaluator.Eval(program,env);
    }

    public static void printParserErrors(ArrayList<String> errors) {
        System.out.println("Whoops! We ran into some chi business here!");
        System.out.println(" parser errors:");
        for(var s : errors) {
            System.out.println("\t" + s);
        }
    }
}
