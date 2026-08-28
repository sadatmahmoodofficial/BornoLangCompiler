lexer grammar BanglaLexer;

// Keywords
DHORI           : 'ধরি' ;
TYPE_INT        : 'সংখ্যা' ;
TYPE_BOOL       : 'সত্যমিথ্যা' ;
JODI            : 'যদি' ;
OTHOBAL_JODI    : 'অথবা_যদি' ;
OTHOBA          : 'অথবা' ;
JOTOKKHON       : 'যতক্ষণ' ;
DEKHAO          : 'দেখাও' ;

// Boolean Literals
BOOL_LITERAL    : 'সত্য' | 'মিথ্যা' ;

// Operators
ADD             : '+' ;
SUB             : '-' ;
MUL             : '*' ;
DIV             : '/' ;
MOD             : '%' ;

EQ              : '==' ;
NEQ             : '!=' ;
LE              : '<=' ;
GE              : '>=' ;
LT              : '<' ;
GT              : '>' ;

ASSIGN          : '=' ;

AND             : 'এবং' | '&&' ;
OR              : 'অথবা_বা' | '||' ;
NOT             : 'না' | '!' ;

// Punctuation
SEMI            : ';' | '।' ;
LPAREN          : '(' ;
RPAREN          : ')' ;
LBRACE          : '{' ;
RBRACE          : '}' ;

// Digits and Identifiers
INT_LITERAL     : [0-9]+ | [\u09E6-\u09EF]+ ;
IDENTIFIER      : [a-zA-Z_\u0980-\u09FF] [a-zA-Z0-9_\u0980-\u09FF]* ;

// Skip Whitespace and Comments
WS              : [ \t\r\n]+ -> skip ;
LINE_COMMENT    : '//' ~[\r\n]* -> skip ;