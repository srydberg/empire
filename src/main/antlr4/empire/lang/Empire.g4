grammar Empire;
prog
  : binding*
    statement*
    EOF
  ;
binding
  : 'bind' Identifier '=' Identifier
  ;
statement
  : '(' condition* ')' outputBlock
  ;
condition
  : Identifier '=' literal
  ;
literal
  : Identifier
  | RegexLiteral
  ; 
outputBlock
  : OutputBlock
  ;

Identifier
  : Letter (Letter|Digit|Hyphen)*
  ;
fragment
Letter
  : [a-zA-Z_]
  ;
fragment
Digit
  : [0-9]
  ;
fragment
Hyphen
  : '-'
  ;
RegexLiteral
  : '~' '/' (EscapeSequence | ~('/'))* '/'
  ;
fragment
EscapeSequence
  : '\/'
  ;
OutputBlock
  : '{' .*? NewLine '}'
  ;
fragment
NewLine
  : ('\r'? '\n')
  ;
Whitespace
  : [ \r\n\t\u000c]+ -> skip
  ;
Comment
  : '#' ~[\r\n]* ('\r'? '\n' | EOF) -> skip
  ;