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
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // Set both standard output and standard error to UTF-8
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        String inputFile = args.length > 0 ? args[0] : "test.bn";
        CharStream input = CharStreams.fromPath(Paths.get(inputFile), StandardCharsets.UTF_8);

        // 1. Lexing & Parsing with Custom Error Listener
        BanglaLexer lexer = new BanglaLexer(input);
        BanglaErrorListener errorListener = new BanglaErrorListener();

        // Remove default console listeners and attach custom one
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BanglaParser parser = new BanglaParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree tree = parser.program();

        // Check for syntax errors before running semantic analysis
        if (errorListener.hasErrors()) {
            System.err.println("======================================");
            System.err.println("    সিনট্যাক্স ত্রুটিসমূহ (Syntax Errors)  ");
            System.err.println("======================================");
            for (String err : errorListener.getSyntaxErrors()) {
                System.err.println("❌ " + err);
            }
            System.err.println("======================================");
            System.err.println("কম্পাইলেশন বন্ধ করা হলো। অনুগ্রহ করে সিনট্যাক্স ঠিক করুন।");
            return;
        }

        // 2. Semantic Analysis & Type Checking Pass
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.visit(tree);

        if (semanticAnalyzer.hasErrors()) {
            System.err.println("======================================");
            System.err.println("    সিম্যান্টিক ত্রুটিসমূহ (Semantic Errors) ");
            System.err.println("======================================");
            for (String err : semanticAnalyzer.getErrors()) {
                System.err.println("❌ " + err);
            }
            System.err.println("======================================");
            System.err.println("কম্পাইলেশন ব্যর্থ হয়েছে। অনুগ্রহ করে কোডের ত্রুটি সংশোধন করুন।");
            return; // Abort code generation if semantic check fails
        }

        System.out.println("✅ সিম্যান্টিক অ্যানালাইসিস সফল হয়েছে (Semantic Analysis Passed)!\n");

        // 3. Generate Intermediate Code (Three-Address Code - TAC)
        TACGenerator tacGen = new TACGenerator();
        tacGen.visit(tree);
        List<String> rawTacInstructions = tacGen.getInstructions();
        String rawTacCode = String.join("\n", rawTacInstructions);

        System.out.println("======================================");
        System.out.println("   RAW THREE-ADDRESS CODE (TAC)       ");
        System.out.println("======================================");
        System.out.println(rawTacCode);
        System.out.println("======================================\n");

        // 4. Intermediate Code Optimization Pass (Constant Folding & Propagation)
        TACOptimizer optimizer = new TACOptimizer();
        List<String> optimizedInstructions = optimizer.optimize(rawTacInstructions);
        String optimizedTacCode = String.join("\n", optimizedInstructions);

        System.out.println("======================================");
        System.out.println("   OPTIMIZED THREE-ADDRESS CODE (TAC) ");
        System.out.println("   [Constant Folding & Dead Code]     ");
        System.out.println("======================================");
        System.out.println(optimizedTacCode);
        System.out.println("======================================\n");

        Files.writeString(Paths.get("tac_output.txt"), optimizedTacCode, StandardCharsets.UTF_8);
        System.out.println("Saved Optimized TAC to: tac_output.txt\n");

        // 5. // Open GUI Parse Tree Window with Bangla Font & Clean Spacing
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BornoLang - Parse Tree Inspector");
            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);

            // 1. Crisp Bangla font with slightly smaller size to prevent overlap
            viewer.setFont(new Font("Nirmala UI", Font.PLAIN, 13));

            // 2. Adjust node dimensions and branch spacing
            viewer.setScale(1.1); // Slight zoom for readability
            
            JScrollPane scrollPane = new JScrollPane(viewer);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            frame.add(scrollPane);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        // 6. Transpile AST to Target Python Code
        BornoToPythonVisitor transpiler = new BornoToPythonVisitor();
        String pythonCode = transpiler.visit(tree);

        // 7. Output to Console & Save output.py
        System.out.println("======================================");
        System.out.println("    GENERATED PYTHON TARGET CODE       ");
        System.out.println("======================================");
        System.out.println(pythonCode);
        System.out.println("======================================\n");

        Files.writeString(Paths.get("output.py"), pythonCode, StandardCharsets.UTF_8);
        System.out.println("Saved compiled target to: output.py");
    }
}