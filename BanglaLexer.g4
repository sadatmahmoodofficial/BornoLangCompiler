lexer grammar BanglaLexer;

// Keywords
DHORI       : 'ধরি' ;
SONKHYA     : 'সংখ্যা' ;
SOTTO_MITTHA: 'সত্যমিথ্যা' ;
BAKKYO      : 'বাক্য' ;
NAO         : 'নাও' ;
JODI        : 'যদি' ;
OTHOBAL_JODI: 'অথবা_যদি' ;
OTHOBA      : 'অথবা' ;
JOTOKKHON   : 'যতক্ষণ' ;
DEKHAO      : 'দেখাও' ;

// Literals
SOTTO       : 'সত্য' ;
MITTHA      : 'মিথ্যা' ;

STRING_LITERAL : '"' (~["\\\r\n] | '\\' .)* '"' ;
INT_LITERAL    : [0-9]+ | [\u09E6-\u09EF]+ ;

// Operators
ASSIGN      : '=' ;
PLUS        : '+' ;
MINUS       : '-' ;
STAR        : '*' ;
SLASH       : '/' ;
MOD         : '%' ;

EQ          : '==' ;
NEQ         : '!=' ;
LTE         : '<=' ;
GTE         : '>=' ;
LT          : '<' ;
GT          : '>' ;

AND         : 'এবং' ;
OR          : 'অথবা_বা' ;
NOT         : 'না' ;

// Delimiters
LPAREN      : '(' ;
RPAREN      : ')' ;
LBRACE      : '{' ;
RBRACE      : '}' ;
COMMA       : ',' ;
SEMI        : ';' | '।' ;

// Identifiers
IDENTIFIER  : [a-zA-Z_\u0980-\u09FF][a-zA-Z0-9_\u0980-\u09FF]* ;

// Whitespace and Comments
WS          : [ \t\r\n]+ -> skip ;
LINE_COMMENT: '//' ~[\r\n]* -> skip ;