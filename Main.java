import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        String inputFile = args.length > 0 ? args[0] : "test.bn";
        CharStream input =  CharStreams.fromPath(Paths.get(inputFile), StandardCharsets.UTF_8);

        // 1. Lexing & Parsing
        BanglaLexer lexer = new BanglaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BanglaParser parser = new BanglaParser(tokens);
        ParseTree tree = parser.program();

        // 2. Transpile BornoLang AST to Target Python Code
        BornoToPythonVisitor transpiler = new BornoToPythonVisitor();
        String pythonCode = transpiler.visit(tree);

        // 3. Print Generated Target Output
        System.out.println("======================================");
        System.out.println("   GENERATED PYTHON TARGET CODE       ");
        System.out.println("======================================");
        System.out.println(pythonCode);
        System.out.println("======================================\n");

        // 4. Save to output.py
        Files.writeString(Paths.get("output.py"), pythonCode, StandardCharsets.UTF_8);
        System.out.println("Saved compiled target to: output.py");
    }
}