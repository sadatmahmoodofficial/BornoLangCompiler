import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;
import javax.swing.*;
import java.awt.Font;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        String inputFile = "test.bn";
        if (args.length > 0) inputFile = args[0];

        CharStream input = CharStreams.fromPath(Paths.get(inputFile), StandardCharsets.UTF_8);

        // 1. Lexer & Parser
        BanglaLexer lexer = new BanglaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BanglaParser parser = new BanglaParser(tokens);
        ParseTree tree = parser.program();

        // 2. Terminal Output
        System.out.println("Lexical & Syntax Parsing Successful!\n");
        System.out.println("Parse Tree:\n" + tree.toStringTree(parser));

        // 3. Custom GUI Window with Bangla Font Support
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("BornoLang - Parse Tree Inspector");
            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);

            // Set explicit font supporting Bengali Unicode
            viewer.setFont(new Font("Nirmala UI", Font.PLAIN, 15));

            JScrollPane scrollPane = new JScrollPane(viewer);
            frame.add(scrollPane);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}