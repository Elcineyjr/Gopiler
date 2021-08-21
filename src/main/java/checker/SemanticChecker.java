package checker;

import org.antlr.v4.runtime.Token;

import parser.GoParser;
import parser.GoParserBaseVisitor;
import tables.FuncTable;
import tables.StrTable;
import tables.VarTable;
import typing.Type;

// TODOs:
// Must do:
//  - Handle arrays (do i need to create and ARRAY_TYPE or so?)
// 	- functions vars scope (not priority for now)
// 	-
// Would be great if done:
// - implement tables using hash
// - maybe move the typeErrors and other functions to typing package
// - 



public class SemanticChecker extends GoParserBaseVisitor<Type> {

	private StrTable st = new StrTable(); // Tabela de strings.
	private VarTable vt = new VarTable(); // Tabela de variáveis.
	private FuncTable ft = new FuncTable(); // Tabela de variáveis.

	Type lastDeclType; // Global variable with the last declared var type 
	Type lastDeclFuncType; // Global variable with the last declared func type 
	int lastDeclArgsSize; // Global variable with the last declared argsSize 
	int lastExpressionListSize;

	private boolean passed = true;

	boolean hasPassed() {
		return passed;
	}

	void printTables() {
		System.out.print("\n\n");
		System.out.print(st);
		System.out.print("\n\n");
		System.out.print(vt);
		System.out.print("\n\n");
		System.out.print(ft);
		System.out.print("\n\n");
	}

	/*------------------------------------------------------------------------------*
	 *	Var checking and declaration.
	 *------------------------------------------------------------------------------*/

	// Checks whether the variable was previously declared
	Type checkVar(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = vt.lookupVar(text);
		if (idx == -1) {
			System.err.printf("SEMANTIC ERROR (%d): variable '%s' was not declared.\n", line, text);
			passed = false;
			return Type.NO_TYPE;
		}
		return vt.getType(idx);
	}

	// Creates a new variable from token
	void newVar(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = vt.lookupVar(text);
		if (idx != -1) {
			System.err.printf("SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
				line, text, vt.getLine(idx)
			);
			passed = false;
			return;
		}
		vt.addVar(text, line, lastDeclType);
	}

	/*------------------------------------------------------------------------------*
	 *	Function checking and declaration.
	 *------------------------------------------------------------------------------*/

	// Checks whether the function was previously declared
	Type checkFunc(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = ft.lookupFunc(text);
		if (idx == -1) {
			System.err.printf("SEMANTIC ERROR (%d): function '%s' was not declared.\n", line, text);
			passed = false;
			return Type.NO_TYPE;
		}
		return ft.getType(idx);
	}

	// Creates a new function from token
	void newFunc(Token token, int argsSize) {
		String text = token.getText();
		int line = token.getLine();
		int idx = ft.lookupFunc(text);
		if (idx != -1) {
			System.err.printf("SEMANTIC ERROR (%d): function '%s' already declared at line %d.\n",
				line, text, ft.getLine(idx)
			);
			passed = false;
			return;
		}
		ft.addFunc(text, line, lastDeclFuncType, argsSize);
	}

	// Checks if the function was called with the right amount of arguments
	// Warning: this function should only be called after an explict call of the checkFunc
	void checkFuncCall(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = ft.lookupFunc(token.getText());

		// Doesnt show 'function not declared' error
		if(idx != -1) {
			int argsSize = ft.getArgsSize(idx);
	
			if(argsSize != lastExpressionListSize) {
				System.err.printf("SEMANTIC ERROR (%d): function '%s' expected %d arguments but received '%d'.\n",
					line, text, argsSize, lastExpressionListSize
				);
				passed = false;
			}
		}
	}

	// Checks if a return statement has a compatible type with the function
	void checkFuncReturnType(int lineNo, Type t) {
		if(t != lastDeclFuncType) {
			System.err.printf(
				"SEMANTIC ERROR (%d): Return statement type incompatible with function type. Expected '%s' but received '%s'.\n",
				lineNo, lastDeclFuncType, t
			);
			passed = false;
		}
	}

    /*------------------------------------------------------------------------------*
	 *	Type, operations and expression checking
	 *------------------------------------------------------------------------------*/
	
    private void typeError(int lineNo, String op, Type t1, Type t2) {
    	System.out.printf(
			"SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
			lineNo, op, t1.toString(), t2.toString()
		);
    	passed = false;
    }

	private void checkUnaryOp(int lineNo, String op, Type t) {
		if (t != Type.INT_TYPE && t != Type.FLOAT32_TYPE) {
			System.out.printf(
				"SEMANTIC ERROR (%d): type '%s' not suported for unary operator '%s'.\n",
				lineNo, t.toString(), op
			);
    		passed = false;
		}
	}

	private void checkAssign(int lineNo, String op,Type l, Type r) {
        if (l == Type.BOOL_TYPE && r != Type.BOOL_TYPE) typeError(lineNo, op, l, r);
        if (l == Type.STRING_TYPE  && r != Type.STRING_TYPE)  typeError(lineNo, op, l, r);
        if (l == Type.INT_TYPE  && r != Type.INT_TYPE)  typeError(lineNo, op, l, r);
        if (l == Type.FLOAT32_TYPE && !(r == Type.INT_TYPE || r == Type.FLOAT32_TYPE)) typeError(lineNo, op, l, r);
    }

	private void checkBoolExpr(int lineNo, String cmd, Type t) {
		if (t != Type.BOOL_TYPE) {
			System.out.printf(
				"SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
				lineNo, cmd, t.toString(), Type.BOOL_TYPE.toString()
			);
			passed = false;
		}
	}

	private void checkIndex(int lineNo, Type t) {
		if(t != Type.INT_TYPE) {
			System.out.printf(
				"SEMANTIC ERROR (%d): incompatible type '%s' at array index.\n",
				lineNo, t.toString()
			);
			passed = false;
		}
    }

	private void checkCase(int lineNo, Type t1, Type t2) {
		if(t1 != t2) {
			System.out.printf(
				"SEMANTIC ERROR (%d): incompatible types for case, expected '%s' but expression is '%s'.\n",
				lineNo, t1.toString(), t2.toString()
			);
			passed = false;
		}
	}

	// ----- Specific for when declaring variables

	private void typeInitError(int lineNo, String varName, Type t1, Type t2) {
    	System.out.printf(
			"SEMANTIC ERROR (%d): incompatible types when declaring variable '%s', var type is '%s' and expression type is '%s'.\n",
			lineNo, varName, t1.toString(), t2.toString()
		);
    	passed = false;
    }

	private void checkInitAssign(int lineNo, String varName, Type l, Type r) {
        if (l == Type.BOOL_TYPE && r != Type.BOOL_TYPE) typeInitError(lineNo, varName, l, r);
        if (l == Type.STRING_TYPE  && r != Type.STRING_TYPE)  typeInitError(lineNo, varName, l, r);
        if (l == Type.INT_TYPE  && r != Type.INT_TYPE)  typeInitError(lineNo, varName, l, r);
        if (l == Type.FLOAT32_TYPE && !(r == Type.INT_TYPE || r == Type.FLOAT32_TYPE)) typeInitError(lineNo, varName, l, r);
    }
    
	/*------------------------------------------------------------------------------*
	 *	Visitors for var_types rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule var_types: INT
	@Override
	public Type visitIntType(GoParser.IntTypeContext ctx) {
		this.lastDeclType = Type.INT_TYPE;
		return Type.NO_TYPE;
	}

	// Visits the rule var_types: STRING
	@Override
	public Type visitStringType(GoParser.StringTypeContext ctx) {
		this.lastDeclType = Type.STRING_TYPE;
		return Type.NO_TYPE;
	}

	// Visits the rule var_types: BOOL
	@Override
	public Type visitBoolType(GoParser.BoolTypeContext ctx) {
		this.lastDeclType = Type.BOOL_TYPE;
		return Type.NO_TYPE;
	}
	
	// Visits the rule var_types: FLOAT32
	@Override
	public Type visitFloat32Type(GoParser.Float32TypeContext ctx) {
		this.lastDeclType = Type.FLOAT32_TYPE;
		return Type.NO_TYPE;
	}

	/*------------------------------------------------------------------------------*
	 *Visitors for var_declaration and declare_assign rules
	 *------------------------------------------------------------------------------*/

	// Visits the rule var_declaration: VAR IDENTIFIER (var_types | var_types? ASSIGN  expression | array_declaration) SEMI?
	@Override
	public Type visitVar_declaration(GoParser.Var_declarationContext ctx) {	
		// Check wheter the variable is an array or a normal variable
		if(ctx.array_declaration() != null) {
			// Recursively visits the array_declaration to define the array type
			visit(ctx.array_declaration());
		} else {
			boolean hasVarType = ctx.var_types() != null;

			// Defines the var type based on the explicit type declaration or 
			// the given initial expression.
			// e.g: var x int
			// 		var x = 10
			if(hasVarType) {
				// Defines lastDeclType with explicit var type declaration
				visit(ctx.var_types());
			} else {
				// Defines lastDeclType based on expression type
				lastDeclType = visit(ctx.expression());
			}
		}

		Token identifierToken = ctx.IDENTIFIER().getSymbol();

		// Checks if the variable was previously declared
		newVar(identifierToken);
		
		boolean hasAssign = ctx.ASSIGN() != null;
		// Checks if the identifier type and expression type match
		if(hasAssign) {
			Type identifierType = checkVar(identifierToken);
			Type expressionType = visit(ctx.expression());

			checkInitAssign(identifierToken.getLine(), identifierToken.getText(), identifierType, expressionType);
		}

		return Type.NO_TYPE;
	}


	// TODO: handle arrays
	// Visits the rule declare_assign: IDENTIFIER DECLARE_ASSIGN ( array_init | expression) SEMI?
	@Override
	public Type visitDeclare_assign(GoParser.Declare_assignContext ctx) {
		// Defines lastDeclType based on expression type or array initialization
		if(ctx.expression() != null) lastDeclType = visit(ctx.expression());
		if(ctx.array_init() != null) visit(ctx.array_init());

		// Checks if the variable was previously declared
		newVar(ctx.IDENTIFIER().getSymbol());

		return Type.NO_TYPE;
	}

	/*------------------------------------------------------------------------------*
	 *Visitor for array_declaration and array_ags rules
	 *------------------------------------------------------------------------------*/

	// Visits the rule array_declaration: L_BRACKET DECIMAL_LIT R_BRACKET var_types
	@Override
	public Type visitArray_declaration(GoParser.Array_declarationContext ctx) {
		// Defines lastDeclType 
		visit(ctx.var_types());		

		return Type.NO_TYPE;
	}

	//  TODO: handle if the declared array size equals init size
	// @Override
	// public Type visitArray_init(GoParser.Array_initContext ctx) {
	// 	visit(ctx.array_declaration());

	// 	lastDeclArgsSize = ctx.id().size();

	// 	return Type.NO_TYPE;
	// }

	/*------------------------------------------------------------------------------*
	 *	Visitors for input and output
	 *------------------------------------------------------------------------------*/

	//  Visits the rule input: INPUT L_PAREN AMPERSAND id R_PAREN
	@Override
	public Type visitInput(GoParser.InputContext ctx) {
		// Recursively visits the rule for error checking
		visit(ctx.id());

		return Type.NO_TYPE;
	}

	// Visits the rule output: OUTPUT L_PAREN expression_list? R_PAREN
	@Override
	public Type visitOutput(GoParser.OutputContext ctx) {
		// Recursively visits the rule for error checking
		visit(ctx.expression_list());

		return Type.NO_TYPE;
	}

	/*------------------------------------------------------------------------------*
	 *	Visitors for functions related rules
	 *------------------------------------------------------------------------------*/
	
	//  TODOs: handle args declaration inside function scope
	// Visits the rule func_declaration: FUNC IDENTIFIER L_PAREN func_args? R_PAREN var_types? statement_section
	@Override
	public Type visitFunc_declaration(GoParser.Func_declarationContext ctx) {
		
		if(ctx.var_types() != null) {
			// Defines lastDeclType 
			visit(ctx.var_types());
		} else {
			// Function has no return type
			lastDeclType = Type.NO_TYPE;
		}

		// Saves the func type before the lastDeclType be overwritten
		// by the args declaration
		lastDeclFuncType = lastDeclType;

		if(ctx.func_args() != null) {
			// Defines lastDeclArgsSize
			visit(ctx.func_args());
		} else {
			// Function has no args
			lastDeclArgsSize = 0;
		}
		
		// Checks if the function was previously declared
		newFunc(ctx.IDENTIFIER().getSymbol(), lastDeclArgsSize);

		// Recursively visits rule for error checking
		visit(ctx.statement_section());

		return Type.NO_TYPE;
	}

	// Visits the rule func_args: id var_types (COMMA id var_types)*
	@Override
	public Type visitFunc_args(GoParser.Func_argsContext ctx) {
		lastDeclArgsSize = ctx.id().size();

		// TODO: add each var into the func var table


		return Type.NO_TYPE;
	}

	// Visits the rule func_section: func_declaration* func_main func_declaration*
	@Override
	public Type visitFunc_section(GoParser.Func_sectionContext ctx) {
		// Checks if there is any other function declaration besides the main function
		if(ctx.func_declaration() != null) {
			// Recursively visits the functions for error checking
			for (GoParser.Func_declarationContext funcDecl : ctx.func_declaration()) {
				visit(funcDecl);
			}
		}

		// Finally visits the main function for error checking
		visit(ctx.func_main());

		return Type.NO_TYPE;
	}

	/*------------------------------------------------------------------------------*
	 *	Visitors for statements rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule statement_section: L_CURLY statement* return_statement? R_CURLY
	@Override
	public Type visitStatement_section(GoParser.Statement_sectionContext ctx) {
		// Recursively visits every statement for error checking
		for (GoParser.StatementContext stmt : ctx.statement()) {
			visit(stmt);
		}

		if(ctx.return_statement() != null) {
			// Recursively visits the rule for error checking
			visit(ctx.return_statement());
		}

		return Type.NO_TYPE;
	}

	// Visits the rule return_statement: RETURN expression SEMI?
	@Override
	public Type visitReturn_statement(GoParser.Return_statementContext ctx) {
		Type expressionType = Type.NO_TYPE;
		
		if(ctx.expression() != null) {
			expressionType = visit(ctx.expression());
		}

		// Checks if the function return type and expression type match
		checkFuncReturnType(ctx.RETURN().getSymbol().getLine(), expressionType);

		return Type.NO_TYPE;
	}

	// Visits the rule if_statement: IF expression statement_section (ELSE statement_section)?
	@Override
	public Type visitIf_statement(GoParser.If_statementContext ctx) {
		Type expressionType = visit(ctx.expression());

		// Checks expression to see if it is bool type 
		checkBoolExpr(ctx.IF().getSymbol().getLine(), "if", expressionType);

		// Recursively visits the statement_section from the if block for error checking
		visit(ctx.statement_section(0));

		// Recursively visits the statement_section from the else block for error checking
		if(ctx.ELSE() != null) {
			visit(ctx.statement_section(1));
		} 

		return Type.NO_TYPE;
	}

	// Visits the rule for_statement: FOR expression? statement_section
	@Override
	public Type visitWhile(GoParser.WhileContext ctx) {
		// Checks if there is a expression
		if(ctx.expression() != null) {
			Type expressionType = visit(ctx.expression());
	
			// Checks if the expression has bool type
			checkBoolExpr(ctx.FOR().getSymbol().getLine(), "for", expressionType);
		}

		// Recursively visits the statement_section for error checking
		visit(ctx.statement_section());

		return Type.NO_TYPE;
	}

	// Visits the rule or_statement: FOR declare_assign SEMI expression SEMI assign_statement statement_section
	@Override
	public Type visitFor(GoParser.ForContext ctx) {
		// Recursively visits rule for error checking
		visit(ctx.declare_assign());

		Type expressionType = visit(ctx.expression());

		// Checks if the expression has bool type
		checkBoolExpr(ctx.FOR().getSymbol().getLine(), "for", expressionType);

		// Recursively visits rules for error checking 
		visit(ctx.assign_statement());
		visit(ctx.statement_section());

		return Type.NO_TYPE;
	}

	// Visits the rule assign_statement: id op=(ASSIGN | MINUS_ASSIGN | PLUS_ASSIGN) expression SEMI?
	@Override
	public Type visitAssignExpression(GoParser.AssignExpressionContext ctx) {
		Type expressionType = visit(ctx.expression());
		
		// Checks if the variable was previously declared
		Type identifierType = visit(ctx.id());
		
		Token identifierToken = ctx.id().IDENTIFIER().getSymbol();

		// Checks if the assign operation is suported
		checkAssign(identifierToken.getLine(), ctx.op.getText(), identifierType, expressionType);

		return Type.NO_TYPE;
	}

	// Visits the rule assign_statement: IDENTIFIER op=(PLUS_PLUS | MINUS_MINUS) SEMI?
	@Override
	public Type visitAssignPPMM(GoParser.AssignPPMMContext ctx) {
		// Checks if the variable was previously declared
		Type identifierType = visit(ctx.id());

		Token identifierToken = ctx.id().IDENTIFIER().getSymbol();

		// Checks if the operation is suported
		checkUnaryOp(identifierToken.getLine(), ctx.op.getText(), identifierType);
		
		// Since the assignment wont change the var type, there is no need 
		// to call the checkAssign function
		return Type.NO_TYPE;
	}

	// Visits the rule switch_statement: SWITCH id? L_CURLY case_statement R_CURLY
	@Override
	public Type visitSwitch_statement(GoParser.Switch_statementContext ctx) {
		// Checks if there is a identifier or func call to be evaluated
		// and recursively visits the rule for error checking
		if(ctx.id() != null) visit(ctx.id());	
		if(ctx.func_call() != null) visit(ctx.func_call());	

		// Recursively visits the case_statement for error checking
		visit(ctx.case_statement());

		return Type.NO_TYPE;
	}

	// Visits the rule case_statement: (CASE expression COLON statement*)* (DEFAULT COLON statement*)?
	@Override
	public Type visitCase_statement(GoParser.Case_statementContext ctx) {
		// Get the parent node of the rule, in this case, the switch node
		GoParser.Switch_statementContext parent = (GoParser.Switch_statementContext) ctx.parent;
		
		// Dont think thats gonna happen, but there it is
		if(parent == null) {
			System.out.println("Case statement has no parent node. Exiting...");
			System.exit(1);
		}

		// Default type for the case expression if nothing is being evaluated
		Type caseType = Type.BOOL_TYPE;

		// Checks if there is a identifier or func_call to be evaluated
		// and set the case type to the same type as the identifier
		if(parent.id() != null) caseType = visit(parent.id());	
		if(parent.func_call() != null) caseType = visit(parent.func_call());	

		// Recursively visits every expression for each case to handle errors
		for(int i = 0; i < ctx.expression().size(); i++) {
			// Get the current expression type
			Type expressionType = visit(ctx.expression(i));
			
			// Check if the expression type matches with the case type
			checkCase(ctx.CASE().get(i).getSymbol().getLine() , caseType, expressionType);
		}

		// TODO: that will probably cause some problems when trying to figure out
		// which statement is from which case
		// Recursively visits every statement for error checking
		for(GoParser.StatementContext stmt : ctx.statement()) {
			visit(stmt);
		}

		// Recursively visits rule for error checking
		if(ctx.default_statement() != null) { 
			visit(ctx.default_statement());
		}

		return Type.NO_TYPE;
	}
	
	//  Visits the rule func_call: IDENTIFIER L_PAREN expression_list? R_PAREN 
	@Override
	public Type visitFunc_call(GoParser.Func_callContext ctx) {
		Token funcToken = ctx.IDENTIFIER().getSymbol();

		// Checks if the function was previously declared
		Type funcType = checkFunc(funcToken);

		// Checks if the function call has any parameters
		if(ctx.expression_list() != null) {
			// Recursively visits rule for error checking
			visit(ctx.expression_list());
		} else {
			lastExpressionListSize = 0;
		}

		// Checks if the expressionListSize is the same size as what the function expects
		checkFuncCall(funcToken);

		return funcType;
	}


	// Visits the rule expression_list: expression (COMMA expression)*
	@Override
	public Type visitExpression_list(GoParser.Expression_listContext ctx) {
		lastExpressionListSize = ctx.expression().size();

		// Recursively visits each expression for error checking
		for(GoParser.ExpressionContext expr : ctx.expression()) {
			visit(expr);
		}

		return Type.NO_TYPE;
	}


	/*------------------------------------------------------------------------------*
	 *	Visitors for expression rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule expression: expression op=(STAR | DIV | MOD) expression
	@Override
	public Type visitStarDivMod(GoParser.StarDivModContext ctx) {
		// Visits both operands to check their types
		Type l = visit(ctx.expression(0));
		Type r = visit(ctx.expression(1));

		// Unify the types from both operands
		Type unif = l.unifyMathOps(r);

		// Operation not allowed
		if (unif == Type.NO_TYPE) {
			typeError(ctx.op.getLine(), ctx.op.getText(), l, r);
		}
		
		return unif;
	}

	// Visits the rule expression: expression op=(PLUS | MINUS) expression
	@Override
	public Type visitPlusMinus(GoParser.PlusMinusContext ctx) {
		// Visits both operands to check their types
		Type l = visit(ctx.expression(0));
		Type r = visit(ctx.expression(1));
		
		// Unify the types from both operands
		Type unif = l.unifyMathOps(r);

		// Operation not suported
		if (unif == Type.NO_TYPE) {
			typeError(ctx.op.getLine(), ctx.op.getText(), l, r);
		}

		return unif;
	}

	// Visits the rule expression: expression op=( EQUALS | NOT_EQUALS | LESS | LESS_OR_EQUALS | GREATER | GREATER_OR_EQUALS) expression
	@Override
	public Type visitRelationalOperators(GoParser.RelationalOperatorsContext ctx) {
		// Visits both operands to check their types
		Type l = visit(ctx.expression(0));
		Type r = visit(ctx.expression(1));
		
		// Unify the types from both operands
		Type unif;
		if(ctx.op.getType() == GoParser.EQUALS || ctx.op.getType() == GoParser.NOT_EQUALS){
			unif = l.unifyCompare(r);
		} else {
			unif = l.unifyCompare2(r);
		}

		// Operation not suported
		if (unif == Type.NO_TYPE) {
			typeError(ctx.op.getLine(), ctx.op.getText(), l, r);
		}

		return unif;
	}

	// Visits the rule expression: L_PAREN expression R_PAREN 
	@Override
	public Type visitExpressionParen(GoParser.ExpressionParenContext ctx) {
		return visit(ctx.expression());
	}

	// Visits the rule expression: id
	@Override
	public Type visitExpressionId(GoParser.ExpressionIdContext ctx) {
		return visit(ctx.id());
	}

	// Visits the rule expression: func_call
	@Override
	public Type visitExpressionFuncCall(GoParser.ExpressionFuncCallContext ctx) {
		return visit(ctx.func_call());
	}

	// Visits the rule expression: DECIMAL_LIT
	@Override
	public Type visitIntVal(GoParser.IntValContext ctx) {
		return Type.INT_TYPE;
	}

	// Visits the rule expression: FLOAT_LIT
	@Override
	public Type visitFloatVal(GoParser.FloatValContext ctx) {
		return Type.FLOAT32_TYPE;
	}

	// Visits the rule expression: INTERPRETED_STRING_LIT
	@Override
	public Type visitStringVal(GoParser.StringValContext ctx) {
		// Adds the string to the string table.
		st.add(ctx.INTERPRETED_STRING_LIT().getText());
		return Type.STRING_TYPE;
	}

	// Visits the rule expression: BOOLEAN_LIT
	@Override
	public Type visitBoolVal(GoParser.BoolValContext ctx) {
		return Type.BOOL_TYPE;
	}

	/*------------------------------------------------------------------------------*
	 *	Visitor for id rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule id: IDENTIFIER (L_BRACKET expression R_BRACKET)?
	@Override
	public Type visitId(GoParser.IdContext ctx) {
		Token identifierToken = ctx.IDENTIFIER().getSymbol();

		// Checks if it has an array index 
		if(ctx.expression() != null) {
			// Recursively visits the expression for error checking
			Type expressionType = visit(ctx.expression());
	
			// Checks if the index is valid
			checkIndex(identifierToken.getLine(), expressionType);
		}

		return checkVar(identifierToken);
	}

}