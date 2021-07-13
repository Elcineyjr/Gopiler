parser grammar GoParser;

options {
  tokenVocab = GoLexer; 
}

program:
  PACKAGE IDENTIFIER import_sect 
;

import_sect: 
  IMPORT package_import
;

package_import: 
  L_PAREN INTERPRETED_STRING_LIT+ R_PAREN
| INTERPRETED_STRING_LIT
;

