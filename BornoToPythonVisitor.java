import java.util.stream.Collectors;

public class BornoToPythonVisitor extends BanglaParserBaseVisitor<String> {
    private int indentLevel = 0;

    private String getIndent() {
        return "    ".repeat(indentLevel);
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
        StringBuilder pyCode = new StringBuilder();
        for (BanglaParser.StatementContext stmt : ctx.statement()) {
            String stmtCode = visit(stmt);
            if (stmtCode != null && !stmtCode.isBlank()) {
                pyCode.append(stmtCode).append("\n");
            }
        }
        return pyCode.toString();
    }

    @Override
    public String visitVarDeclaration(BanglaParser.VarDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String exprCode = ctx.expr() != null ? visit(ctx.expr()) : "None";
        return getIndent() + varName + " = " + exprCode;
    }

    @Override
    public String visitAssignmentStatement(BanglaParser.AssignmentStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String exprCode = visit(ctx.expr());
        return getIndent() + varName + " = " + exprCode;
    }

    @Override
    public String visitPrintStatement(BanglaParser.PrintStatementContext ctx) {
        String exprCode = visit(ctx.expr());
        return getIndent() + "print(" + exprCode + ")";
    }

    @Override
    public String visitIfStatement(BanglaParser.IfStatementContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent()).append("if ").append(visit(ctx.expr(0))).append(":\n");
        
        indentLevel++;
        sb.append(visit(ctx.statement(0)));
        indentLevel--;

        int exprIndex = 1;
        int stmtIndex = 1;

        // অথবা_যদি (elif)
        for (int i = 0; i < ctx.OTHOBAL_JODI().size(); i++) {
            sb.append("\n").append(getIndent()).append("elif ").append(visit(ctx.expr(exprIndex++))).append(":\n");
            indentLevel++;
            sb.append(visit(ctx.statement(stmtIndex++)));
            indentLevel--;
        }

        // অথবা (else)
        if (ctx.OTHOBA() != null) {
            sb.append("\n").append(getIndent()).append("else:\n");
            indentLevel++;
            sb.append(visit(ctx.statement(stmtIndex)));
            indentLevel--;
        }

        return sb.toString();
    }

    @Override
    public String visitWhileStatement(BanglaParser.WhileStatementContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent()).append("while ").append(visit(ctx.expr())).append(":\n");
        indentLevel++;
        sb.append(visit(ctx.statement()));
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visitBlock(BanglaParser.BlockContext ctx) {
        if (ctx.statement().isEmpty()) {
            return getIndent() + "pass\n";
        }
        return ctx.statement().stream()
                .map(this::visit)
                .collect(Collectors.joining("\n"));
    }

    // --- Expression Handlers ---

    @Override
    public String visitAddSubExpr(BanglaParser.AddSubExprContext ctx) {
        return visit(ctx.expr(0)) + " " + ctx.op.getText() + " " + visit(ctx.expr(1));
    }

    @Override
    public String visitMulDivExpr(BanglaParser.MulDivExprContext ctx) {
        return visit(ctx.expr(0)) + " " + ctx.op.getText() + " " + visit(ctx.expr(1));
    }

    @Override
    public String visitRelationalExpr(BanglaParser.RelationalExprContext ctx) {
        return visit(ctx.expr(0)) + " " + ctx.op.getText() + " " + visit(ctx.expr(1));
    }

    @Override
    public String visitEqualityExpr(BanglaParser.EqualityExprContext ctx) {
        return visit(ctx.expr(0)) + " " + ctx.op.getText() + " " + visit(ctx.expr(1));
    }

    @Override
    public String visitLogicalAndExpr(BanglaParser.LogicalAndExprContext ctx) {
        return visit(ctx.expr(0)) + " and " + visit(ctx.expr(1));
    }

    @Override
    public String visitLogicalOrExpr(BanglaParser.LogicalOrExprContext ctx) {
        return visit(ctx.expr(0)) + " or " + visit(ctx.expr(1));
    }

    @Override
    public String visitNotExpr(BanglaParser.NotExprContext ctx) {
        return "not " + visit(ctx.expr());
    }

    @Override
    public String visitParenExpr(BanglaParser.ParenExprContext ctx) {
        return "(" + visit(ctx.expr()) + ")";
    }

    @Override
    public String visitIdExpr(BanglaParser.IdExprContext ctx) {
        return ctx.IDENTIFIER().getText();
    }

    @Override
    public String visitLiteralExpr(BanglaParser.LiteralExprContext ctx) {
        String text = ctx.literal().getText();
        if (text.equals("সত্য")) return "True";
        if (text.equals("মিথ্যা")) return "False";
        return normalizeDigits(text);
    }
}