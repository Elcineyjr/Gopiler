parser grammar GoParser;

options {
  tokenVocab = GoLexer; 
}

program:
  PACKAGE IDENTIFIER import_sect? func
;

import_sect: 
  IMPORT package_import
;

package_import: 
  L_PAREN INTERPRETED_STRING_LIT+ R_PAREN
| INTERPRETED_STRING_LIT
;

func:
  FUNC IDENTIFIER L_PAREN R_PAREN statement_section
;

// Declarations

var_declaration:
  VAR IDENTIFIER (var_types | var_types? ASSIGN (var_value_types | expression) | array_declaration) SEMI?
| IDENTIFIER DECLARE_ASSIGN (var_value_types | array_declaration) SEMI?
;  

array_declaration:
  L_BRACKET DECIMAL_LIT R_BRACKET var_types (L_CURLY ((var_value_types COMMA)* var_value_types)? R_CURLY)?
;

// Statements

statement_section:
  L_CURLY statement* R_CURLY
;

statement:
  var_declaration
| if_statement
| for_statement
| assign_statement
| switch_statement
| case_statement
;

if_statement:
  IF expression statement_section+ (ELSE statement_section+)?
;

for_statement:
  FOR expression? statement_section+
| FOR var_declaration SEMI expression SEMI assign_statement statement_section+
;

assign_statement: 
  IDENTIFIER (L_BRACKET expression R_BRACKET)? (ASSIGN | MINUS_ASSIGN | PLUS_ASSIGN) expression SEMI?
| IDENTIFIER (PLUS_PLUS | MINUS_MINUS) SEMI?
;

switch_statement:
  SWITCH expression? L_CURLY case_statement R_CURLY
;

case_statement: 
  (CASE expression | DEFAULT) COLON statement* 
;

// Expression

expression:
  expression (STAR | DIV | MOD) expression
| expression (PLUS | MINUS) expression
| expression relation_operators expression
| L_PAREN expression R_PAREN
| var_value_types
| IDENTIFIER (L_BRACKET expression R_BRACKET)?
;

// Relation operators

relation_operators:
  EQUALS
| NOT_EQUALS
| LESS
| LESS_OR_EQUALS
| GREATER
| GREATER_OR_EQUALS
;

// Var types and values

var_types: 
  INT
| STRING
| BOOL
;

var_value_types : 
  DECIMAL_LIT 
| BINARY_LIT  
| OCTAL_LIT 
| HEX_LIT
| FLOAT_LIT
| DECIMAL_FLOAT_LIT 
| HEX_FLOAT_LIT
| INTERPRETED_STRING_LIT
| BOOLEAN_LIT
;