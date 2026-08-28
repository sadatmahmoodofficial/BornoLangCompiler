import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;
import javax.swing.*;
import java.awt.Font;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        String inputFile = args.length > 0 ? args[0] : "test.bn";
        CharStream input = CharStreams.fromPath(Paths.get(inputFile), StandardCharsets.UTF_8);

        // 1. Lexing & Parsing
        BanglaLexer lexer = new BanglaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BanglaParser parser = new BanglaParser(tokens);
        ParseTree tree = parser.program();

        // 2. Open GUI Parse Tree Window with Bangla Font Support
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BornoLang - Parse Tree Inspector");
            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
            viewer.setFont(new Font("Nirmala UI", Font.PLAIN, 15));

            JScrollPane scrollPane = new JScrollPane(viewer);
            frame.add(scrollPane);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        // 3. Transpile AST to Target Python Code
        BornoToPythonVisitor transpiler = new BornoToPythonVisitor();
        String pythonCode = transpiler.visit(tree);

        // 4. Output to Console & Save output.py
        System.out.println("======================================");
        System.out.println("   GENERATED PYTHON TARGET CODE       ");
        System.out.println("======================================");
        System.out.println(pythonCode);
        System.out.println("======================================\n");

        Files.writeString(Paths.get("output.py"), pythonCode, StandardCharsets.UTF_8);
        System.out.println("Saved compiled target to: output.py");
    }
}