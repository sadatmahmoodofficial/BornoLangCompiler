import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer extends BanglaParserBaseVisitor<String> {
    private SymbolTable currentScope = new SymbolTable();
    private final List<String> errors = new ArrayList<>();

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    private void addError(int line, int col, String message) {
        errors.add("সিম্যান্টিক ত্রুটি (Semantic Error) [লাইন " + line + ":" + col + "] -> " + message);
    }

    @Override
    public String visitBlock(BanglaParser.BlockContext ctx) {
        // Enter nested scope
        currentScope = new SymbolTable(currentScope);
        super.visitBlock(ctx);
        // Restore parent scope
        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public String visitVarDeclaration(BanglaParser.VarDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String declaredType = ctx.type().getText(); // "সংখ্যা", "সত্যমিথ্যা", or "বাক্য"
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine();

        if (!currentScope.declare(varName, declaredType)) {
            addError(line, col, "ভেরিয়েবল '" + varName + "' ইতিমধ্যেই এই স্কোপে ঘোষণা করা হয়েছে (Already declared).");
        }

        if (ctx.expr() != null) {
            String exprType = visit(ctx.expr());
            if (exprType != null && !exprType.equals("UNKNOWN") && !exprType.equals(declaredType)) {
                addError(line, col, "টাইপ অমিল (Type Mismatch): '" + declaredType + "' হিসেবে ঘোষিত ভেরিয়েবলে '" + exprType + "' মান দেওয়া যাবে না।");
            }
        }
        return null;
    }

    @Override
    public String visitAssignmentStatement(BanglaParser.AssignmentStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine();

        String varType = currentScope.lookup(varName);
        if (varType == null) {
            addError(line, col, "অঘোষিত ভেরিয়েবল '" + varName + "' (Undeclared variable). ব্যবহারের আগে 'ধরি' দিয়ে ঘোষণা করুন।");
        }

        String exprType = visit(ctx.expr());
        if (varType != null && exprType != null && !exprType.equals("UNKNOWN") && !varType.equals(exprType)) {
            addError(line, col, "টাইপ অমিল (Type Mismatch): '" + varType + "' ভেরিয়েবলে '" + exprType + "' অ্যাসাইন করা সম্ভব নয়।");
        }
        return null;
    }

    @Override
    public String visitInputStatement(BanglaParser.InputStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine();

        String varType = currentScope.lookup(varName);
        if (varType == null) {
            addError(line, col, "অঘোষিত ভেরিয়েবল '" + varName + "' (Undeclared variable). ইনপুট নেওয়ার আগে 'ধরি' দিয়ে ঘোষণা করুন।");
        }

        // Validate optional prompt expression if present: x = নাও("নাম লিখুন: ")
        if (ctx.expr() != null) {
            visit(ctx.expr());
        }
        return null;
    }

    @Override
    public String visitIfStatement(BanglaParser.IfStatementContext ctx) {
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine();

        String condType = visit(ctx.expr(0));
        if (condType != null && !condType.equals("সত্যমিথ্যা") && !condType.equals("UNKNOWN")) {
            addError(line, col, "শর্তের টাইপ অবশ্যই 'সত্যমিথ্যা' (Boolean) হতে হবে। পাওয়া গেছে: '" + condType + "'।");
        }
        return super.visitIfStatement(ctx);
    }

    @Override
    public String visitWhileStatement(BanglaParser.WhileStatementContext ctx) {
        int line = ctx.getStart().getLine();
        int col = ctx.getStart().getCharPositionInLine();

        String condType = visit(ctx.expr());
        if (condType != null && !condType.equals("সত্যমিথ্যা") && !condType.equals("UNKNOWN")) {
            addError(line, col, "লুপের শর্ত অবশ্যই 'সত্যমিথ্যা' (Boolean) হতে হবে। পাওয়া গেছে: '" + condType + "'।");
        }
        return super.visitWhileStatement(ctx);
    }

    // --- Expression Type Inferences ---

    @Override
    public String visitAddSubExpr(BanglaParser.AddSubExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));

        // String concatenation strictly when both operands are 'বাক্য'
        if ("+".equals(ctx.op.getText()) && "বাক্য".equals(left) && "বাক্য".equals(right)) {
            return "বাক্য";
        }

        // Numeric addition/subtraction
        if (!"সংখ্যা".equals(left) || !"সংখ্যা".equals(right)) {
            addError(ctx.op.getLine(), ctx.op.getCharPositionInLine(), "গাণিতিক অপারেশন শুধু 'সংখ্যা' টাইপের মধ্যে অথবা '+' দ্বারা দুটি 'বাক্য' এর মধ্যে প্রযোজ্য।");
        }
        return "সংখ্যা";
    }

    @Override
    public String visitMulDivExpr(BanglaParser.MulDivExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        if (!"সংখ্যা".equals(left) || !"সংখ্যা".equals(right)) {
            addError(ctx.op.getLine(), ctx.op.getCharPositionInLine(), "গুণ/ভাগ/ভাগশেষ শুধু 'সংখ্যা' টাইপের মধ্যে প্রযোজ্য।");
        }
        return "সংখ্যা";
    }

    @Override
    public String visitRelationalExpr(BanglaParser.RelationalExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        if (!"সংখ্যা".equals(left) || !"সংখ্যা".equals(right)) {
            addError(ctx.op.getLine(), ctx.op.getCharPositionInLine(), "তুলনামূলক অপারেশন (<, >, <=, >=) শুধু 'সংখ্যা' টাইপের জন্য প্রযোজ্য।");
        }
        return "সত্যমিথ্যা";
    }

    @Override
    public String visitEqualityExpr(BanglaParser.EqualityExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        if (left != null && right != null && !left.equals("UNKNOWN") && !right.equals("UNKNOWN") && !left.equals(right)) {
            addError(ctx.op.getLine(), ctx.op.getCharPositionInLine(), "ভিন্ন টাইপের মধ্যে সমতা চেক করা যাবে না: '" + left + "' ও '" + right + "'।");
        }
        return "সত্যমিথ্যা";
    }

    @Override
    public String visitLogicalAndExpr(BanglaParser.LogicalAndExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        if (!"সত্যমিথ্যা".equals(left) || !"সত্যমিথ্যা".equals(right)) {
            addError(ctx.AND().getSymbol().getLine(), ctx.AND().getSymbol().getCharPositionInLine(), "লজিক্যাল 'এবং' অপারেশন শুধু 'সত্যমিথ্যা' টাইপের জন্য প্রযোজ্য।");
        }
        return "সত্যমিথ্যা";
    }

    @Override
    public String visitLogicalOrExpr(BanglaParser.LogicalOrExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        if (!"সত্যমিথ্যা".equals(left) || !"সত্যমিথ্যা".equals(right)) {
            addError(ctx.OR().getSymbol().getLine(), ctx.OR().getSymbol().getCharPositionInLine(), "লজিক্যাল 'অথবা_বা' অপারেশন শুধু 'সত্যমিথ্যা' টাইপের জন্য প্রযোজ্য।");
        }
        return "সত্যমিথ্যা";
    }

    @Override
    public String visitNotExpr(BanglaParser.NotExprContext ctx) {
        String operand = visit(ctx.expr());
        if (!"সত্যমিথ্যা".equals(operand)) {
            addError(ctx.NOT().getSymbol().getLine(), ctx.NOT().getSymbol().getCharPositionInLine(), "লজিক্যাল 'না' অপারেশন শুধু 'সত্যমিথ্যা' টাইপের জন্য প্রযোজ্য।");
        }
        return "সত্যমিথ্যা";
    }

    @Override
    public String visitParenExpr(BanglaParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public String visitIdExpr(BanglaParser.IdExprContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String type = currentScope.lookup(varName);
        if (type == null) {
            addError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "অঘোষিত ভেরিয়েবল '" + varName + "' প্রকাশনায় ব্যবহৃত হয়েছে।");
            return "UNKNOWN";
        }
        return type;
    }

    @Override
    public String visitLiteralExpr(BanglaParser.LiteralExprContext ctx) {
        if (ctx.literal().INT_LITERAL() != null) {
            return "সংখ্যা";
        }
        return "সত্যমিথ্যা";
    }

    @Override
    public String visitStringExpr(BanglaParser.StringExprContext ctx) {
        return "বাক্য";
    }
}