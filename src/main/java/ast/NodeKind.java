package ast;

public enum NodeKind {

    /*---------------------------------*
	 *	Value nodes
	 *---------------------------------*/

    BOOL_VAL_NODE,
    INT_VAL_NODE,
    FLOAT32_VAL_NODE,
    STRING_VAL_NODE,

    /*---------------------------------*
	 *	I/O nodes
	 *---------------------------------*/

    INPUT_NODE,
    OUTPUT_NODE,

    /*---------------------------------*
	 *	Relational nodes
	 *---------------------------------*/

    EQUALS_NODE,
    NOT_EQUALS_NODE,
    LESS_NODE,
    LESS_OR_EQUALS_NODE,
    GREATER_NODE,
    GREATER_OR_EQUALS_NODE,

    /*---------------------------------*
	 *	Arithmetics nodes
	 *---------------------------------*/

    STAR_NODE,
    DIV_NODE,
    MOD_NODE,
    PLUS_NODE,
    MINUS_NODE,

    /*---------------------------------*
	 *	Statement nodes
	 *---------------------------------*/
    
    STATEMENT_SECTION_NODE,
    VAR_DECL_NODE,
    DECLARE_ASSIGN_NODE,
    ASSIGN_NODE,
    IF_NODE,
    FOR_NODE,
    SWITCH_NODE,
    CASE_NODE,
    DEFAULT_NODE,
    FUNC_CALL_NODE,

    /*---------------------------------*
	 *	Function nodes
	 *---------------------------------*/
    
    FUNC_MAIN_NODE,
    FUNC_DECL_NODE,
    
    // ----------------------------------------------------

    PROGRAM_NODE,
    FUNC_LIST_NODE,
    VAR_LIST_NODE, // TODO: is it really necessary?
    VAR_USE_NODE;

    

    @Override
  	public String toString() {
		switch(this) {
            case BOOL_VAL_NODE:
            case INT_VAL_NODE:
            case FLOAT32_VAL_NODE:
            case STRING_VAL_NODE:
			    return "";

            case INPUT_NODE:    return "input";
            case OUTPUT_NODE:   return "output";

            case EQUALS_NODE:               return "==";
            case NOT_EQUALS_NODE:           return "!=";
            case LESS_NODE:                 return "<";
            case LESS_OR_EQUALS_NODE:       return "<=";
            case GREATER_NODE:              return ">";
            case GREATER_OR_EQUALS_NODE:    return ">=";

            case STAR_NODE:     return "*";
            case DIV_NODE:      return "/";
            case MOD_NODE:      return "%";
            case PLUS_NODE:     return "+";
            case MINUS_NODE:    return "-";

            case VAR_DECL_NODE:             return "var_decl";            
            case DECLARE_ASSIGN_NODE:       return ":=";
            case ASSIGN_NODE:               return "=";
            case IF_NODE:                   return "if";
            case FOR_NODE:                  return "for";
            case SWITCH_NODE:               return "switch";
            case CASE_NODE:                 return "case";
            case DEFAULT_NODE:              return "default";
            case FUNC_CALL_NODE:            return "func_call";
            case STATEMENT_SECTION_NODE:    return "statement_sect";
            
            case FUNC_MAIN_NODE:    return "func_main";
            case FUNC_DECL_NODE:    return "func_decl";

            case PROGRAM_NODE:      return "program";
            case FUNC_LIST_NODE:    return "func_list";
            case VAR_LIST_NODE:     return "var_list"; // TODO: is it really necessary?
            case VAR_USE_NODE:      return "var_use";
            
            default: return "";
    	}
	}
	
	public static boolean hasData(NodeKind kind) {
		switch(kind) {
	        case BOOL_VAL_NODE:
	        case INT_VAL_NODE:
	        case STRING_VAL_NODE:
	        case FLOAT32_VAL_NODE:
	        case VAR_DECL_NODE:
            case DECLARE_ASSIGN_NODE:
	        case VAR_USE_NODE:
	            return true;
	        default:
	            return false;
		}
	}
}
