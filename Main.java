import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        String inputFile = "test.bn";
        if (args.length > 0) inputFile = args[0];

        CharStream input = CharStreams.fromPath(Paths.get(inputFile), StandardCharsets.UTF_8);

        // 1. Lexer
        BanglaLexer lexer = new BanglaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 2. Parser
        BanglaParser parser = new BanglaParser(tokens);
        ParseTree tree = parser.program();

        // 3. Output
        System.out.println("Lexical & Syntax Parsing Successful!\n");
        System.out.println("Parse Tree:\n" + tree.toStringTree(parser));
    }
}