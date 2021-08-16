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
  VAR IDENTIFIER (var_types | var_types? ASSIGN  expression | array_declaration) SEMI?    #varDeclaration
| IDENTIFIER DECLARE_ASSIGN ( array_declaration | expression) SEMI?                       #declareAssign
;  

array_declaration:
  L_BRACKET DECIMAL_LIT R_BRACKET var_types (L_CURLY array_args? R_CURLY)?
;

array_args:
  array_args COMMA array_args
| expression
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
  expression (STAR | DIV | MOD) expression        #starDivMod
| expression (PLUS | MINUS) expression            #plusMinus
| expression relational_operators expression      #relationalOperators
| L_PAREN expression R_PAREN                      #expressionParen
| IDENTIFIER (L_BRACKET expression R_BRACKET)?    #identifier
| func_call                                       #funcCall
| DECIMAL_LIT                                     #intVal
| BINARY_LIT                                      #binaryVal
| OCTAL_LIT                                       #octalVal
| HEX_LIT                                         #hexVal
| FLOAT_LIT                                       #floatVal
| DECIMAL_FLOAT_LIT                               #decimalFloatVal
| HEX_FLOAT_LIT                                   #hexFloatVal
| INTERPRETED_STRING_LIT                          #stringVal
| BOOLEAN_LIT                                     #boolVal
;

// Relational operators

relational_operators:
  EQUALS
| NOT_EQUALS
| LESS
| LESS_OR_EQUALS
| GREATER
| GREATER_OR_EQUALS
;

// Var types

var_types: 
  INT         #intType
| STRING      #stringType
| BOOL        #boolType
| FLOAT32     #float32Type
;
