import java.util.ArrayList;
import java.util.List;

public class TACGenerator extends BanglaParserBaseVisitor<String> {
    private int tempCount = 1;
    private int labelCount = 1;
    private final List<String> instructions = new ArrayList<>();

    private String newTemp() {
        return "t" + (tempCount++);
    }

    private String newLabel() {
        return "L" + (labelCount++);
    }

    private void emit(String instruction) {
        instructions.add(instruction);
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public String getTACCode() {
        return String.join("\n", instructions);
    }

    // Convert Bangla digits (০-৯) to standard digits (0-9)
    private String normalizeDigits(String text) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch >= '\u09E6' && ch <= '\u09EF') {
                sb.append((char) ('0' + (ch - '\u09E6')));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    @Override
    public String visitProgram(BanglaParser.ProgramContext ctx) {
        for (BanglaParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        return getTACCode();
    }

    @Override
    public String visitVarDeclaration(BanglaParser.VarDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        if (ctx.expr() != null) {
            String exprVal = visit(ctx.expr());
            emit(varName + " = " + exprVal);
        }
        return null;
    }

    @Override
    public String visitAssignmentStatement(BanglaParser.AssignmentStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String exprVal = visit(ctx.expr());
        emit(varName + " = " + exprVal);
        return null;
    }

    @Override
    public String visitInputStatement(BanglaParser.InputStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String prompt = ctx.expr() != null ? visit(ctx.expr()) : "\"\"";
        emit("PARAM " + prompt);
        emit("CALL input, 1");
        emit(varName + " = RET");
        return null;
    }

    @Override
    public String visitPrintStatement(BanglaParser.PrintStatementContext ctx) {
        String exprVal = visit(ctx.expr());
        emit("PARAM " + exprVal);
        emit("CALL print, 1");
        return null;
    }

    @Override
    public String visitIfStatement(BanglaParser.IfStatementContext ctx) {
        String condVal = visit(ctx.expr(0));
        String elseLabel = newLabel();
        String endLabel = newLabel();

        emit("IF_FALSE " + condVal + " GOTO " + elseLabel);
        visit(ctx.statement(0));
        emit("GOTO " + endLabel);
        emit(elseLabel + ":");

        int exprIdx = 1;
        int stmtIdx = 1;

        // অথবা_যদি (elif)
        for (int i = 0; i < ctx.OTHOBAL_JODI().size(); i++) {
            String nextElifLabel = newLabel();
            String elifCond = visit(ctx.expr(exprIdx++));
            emit("IF_FALSE " + elifCond + " GOTO " + nextElifLabel);
            visit(ctx.statement(stmtIdx++));
            emit("GOTO " + endLabel);
            emit(nextElifLabel + ":");
        }

        // অথবা (else)
        if (ctx.OTHOBA() != null) {
            visit(ctx.statement(stmtIdx));
        }

        emit(endLabel + ":");
        return null;
    }

    @Override
    public String visitWhileStatement(BanglaParser.WhileStatementContext ctx) {
        String startLabel = newLabel();
        String endLabel = newLabel();

        emit(startLabel + ":");
        String condVal = visit(ctx.expr());
        emit("IF_FALSE " + condVal + " GOTO " + endLabel);
        visit(ctx.statement());
        emit("GOTO " + startLabel);
        emit(endLabel + ":");
        return null;
    }

    @Override
    public String visitBlock(BanglaParser.BlockContext ctx) {
        for (BanglaParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    // --- Expression TAC Logic (Linearizes nested computations) ---

    @Override
    public String visitAddSubExpr(BanglaParser.AddSubExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        String temp = newTemp();
        emit(temp + " = " + left + " " + ctx.op.getText() + " " + right);
        return temp;
    }

    @Override
    public String visitMulDivExpr(BanglaParser.MulDivExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        String temp = newTemp();
        emit(temp + " = " + left + " " + ctx.op.getText() + " " + right);
        return temp;
    }

    @Override
    public String visitRelationalExpr(BanglaParser.RelationalExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        String temp = newTemp();
        emit(temp + " = " + left + " " + ctx.op.getText() + " " + right);
        return temp;
    }

    @Override
    public String visitEqualityExpr(BanglaParser.EqualityExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        String temp = newTemp();
        emit(temp + " = " + left + " " + ctx.op.getText() + " " + right);
        return temp;
    }

    @Override
    public String visitLogicalAndExpr(BanglaParser.LogicalAndExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        String temp = newTemp();
        emit(temp + " = " + left + " AND " + right);
        return temp;
    }

    @Override
    public String visitLogicalOrExpr(BanglaParser.LogicalOrExprContext ctx) {
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        String temp = newTemp();
        emit(temp + " = " + left + " OR " + right);
        return temp;
    }

    @Override
    public String visitNotExpr(BanglaParser.NotExprContext ctx) {
        String operand = visit(ctx.expr());
        String temp = newTemp();
        emit(temp + " = NOT " + operand);
        return temp;
    }

    @Override
    public String visitParenExpr(BanglaParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public String visitIdExpr(BanglaParser.IdExprContext ctx) {
        return ctx.IDENTIFIER().getText();
    }

    @Override
    public String visitLiteralExpr(BanglaParser.LiteralExprContext ctx) {
        String text = ctx.literal().getText();
        if (text.equals("সত্য")) return "true";
        if (text.equals("মিথ্যা")) return "false";
        return normalizeDigits(text);
    }

    @Override
    public String visitStringExpr(BanglaParser.StringExprContext ctx) {
        return ctx.STRING_LITERAL().getText();
    }
}