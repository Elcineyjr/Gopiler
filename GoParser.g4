parser grammar GoParser;

options {
  tokenVocab = GoLexer; 
}

program:
  PACKAGE MAIN import_section? func_section
;

func_section: 
  func_declaration* func_main func_declaration*
;

import_section: 
  IMPORT package_import
;

package_import: 
  L_PAREN INTERPRETED_STRING_LIT+ R_PAREN
| INTERPRETED_STRING_LIT
;

func_main:
  FUNC MAIN L_PAREN func_args? R_PAREN var_types? statement_section
;

// Declarations

func_declaration:
  FUNC IDENTIFIER L_PAREN func_args? R_PAREN var_types? statement_section
;

var_declaration:
  VAR IDENTIFIER (var_types | var_types? ASSIGN (var_value_types | expression) | array_declaration) SEMI?
| IDENTIFIER DECLARE_ASSIGN (var_value_types | array_declaration | expression) SEMI?
;  

array_declaration:
  L_BRACKET DECIMAL_LIT R_BRACKET var_types (L_CURLY array_args? R_CURLY)?
;

array_args:
  array_args COMMA array_args
| var_value_types
;

// Functions

func_args:
  func_args COMMA func_args
| IDENTIFIER (L_BRACKET DECIMAL_LIT R_BRACKET)? var_types
;

func_params:
  func_params COMMA func_params
| expression
;

func_call:
  IDENTIFIER L_PAREN func_params? R_PAREN 
;

// Statements

statement_section:
  L_CURLY statement* (RETURN expression SEMI?)? R_CURLY
;

statement:
  var_declaration
| if_statement
| for_statement
| assign_statement
| switch_statement
| case_statement
| func_call SEMI?
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
| func_call
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
| FLOAT32
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