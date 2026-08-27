parser grammar BanglaParser;

options {
    tokenVocab = BanglaLexer;
}

program
    : statement* EOF
    ;

statement
    : varDeclaration
    | assignmentStatement
    | ifStatement
    | whileStatement
    | printStatement
    | block
    ;

block
    : LBRACE statement* RBRACE
    ;

varDeclaration
    : DHORI type IDENTIFIER (ASSIGN expr)? SEMI
    ;

type
    : TYPE_INT
    | TYPE_BOOL
    ;

assignmentStatement
    : IDENTIFIER ASSIGN expr SEMI
    ;

ifStatement
    : JODI LPAREN expr RPAREN statement (OTHOBAL_JODI LPAREN expr RPAREN statement)* (OTHOBA statement)?
    ;

whileStatement
    : JOTOKKHON LPAREN expr RPAREN statement
    ;

printStatement
    : DEKHAO LPAREN expr RPAREN SEMI
    ;

expr
    : LPAREN expr RPAREN                     # ParenExpr
    | NOT expr                               # NotExpr
    | expr op=(MUL | DIV | MOD) expr         # MulDivExpr
    | expr op=(ADD | SUB) expr               # AddSubExpr
    | expr op=(LT | GT | LE | GE) expr       # RelationalExpr
    | expr op=(EQ | NEQ) expr                # EqualityExpr
    | expr AND expr                          # LogicalAndExpr
    | expr OR expr                           # LogicalOrExpr
    | literal                                # LiteralExpr
    | IDENTIFIER                             # IdExpr
    ;

literal
    : INT_LITERAL
    | BOOL_LITERAL
    ;