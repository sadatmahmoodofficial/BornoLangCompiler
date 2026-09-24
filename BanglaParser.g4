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
    | printStatement
    | inputStatement
    | ifStatement
    | whileStatement
    | block
    ;

varDeclaration
    : DHORI type IDENTIFIER (ASSIGN expr)? SEMI
    ;

type
    : SONKHYA
    | SOTTO_MITTHA
    | BAKKYO
    ;

assignmentStatement
    : IDENTIFIER ASSIGN expr SEMI
    ;

printStatement
    : DEKHAO LPAREN expr RPAREN SEMI
    ;

inputStatement
    : IDENTIFIER ASSIGN NAO LPAREN expr? RPAREN SEMI
    ;

ifStatement
    : JODI LPAREN expr RPAREN statement (OTHOBAL_JODI LPAREN expr RPAREN statement)* (OTHOBA statement)?
    ;

whileStatement
    : JOTOKKHON LPAREN expr RPAREN statement
    ;

block
    : LBRACE statement* RBRACE
    ;

expr
    : LPAREN expr RPAREN                                    # ParenExpr
    | NOT expr                                              # NotExpr
    | expr op=(STAR | SLASH | MOD) expr                     # MulDivExpr
    | expr op=(PLUS | MINUS) expr                           # AddSubExpr
    | expr op=(LT | GT | LTE | GTE) expr                    # RelationalExpr
    | expr op=(EQ | NEQ) expr                               # EqualityExpr
    | expr AND expr                                         # LogicalAndExpr
    | expr OR expr                                          # LogicalOrExpr
    | IDENTIFIER                                            # IdExpr
    | literal                                               # LiteralExpr
    | STRING_LITERAL                                        # StringExpr
    ;

literal
    : INT_LITERAL
    | SOTTO
    | MITTHA
    ;