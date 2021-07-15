parser grammar GoParser;

options {
  tokenVocab = GoLexer; 
}

program:
  PACKAGE IDENTIFIER import_sect func
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

statement_section:
  L_CURLY statement* R_CURLY
;

statement:
  var_declaration
;

var_declaration:
  VAR IDENTIFIER var_types? ASSIGN var_value_types
| VAR IDENTIFIER var_types
| IDENTIFIER DECLARE_ASSIGN var_value_types
;  

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