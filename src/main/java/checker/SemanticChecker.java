package checker;

import org.antlr.v4.runtime.Token;

import ast.AST;
import ast.NodeKind;
import parser.GoParser;
import parser.GoParserBaseVisitor;
import tables.FuncTable;
import tables.StrTable;
import tables.VarTable;
import typing.Type;

// TODOs:
// Must do:
// 	- vars scope (not priority for now)
// 	-
// Would be great if done:
// - implement tables using hash
// - maybe move the typeErrors and other functions to typing package
// - 



public class SemanticChecker extends GoParserBaseVisitor<AST> {

	private StrTable st = new StrTable(); // Tabela de strings.
	private VarTable vt = new VarTable(); // Tabela de variáveis.
	private FuncTable ft = new FuncTable(); // Tabela de variáveis.

	Type lastDeclType; // Global variable with the last declared var type 
	Type lastDeclFuncType; // Global variable with the last declared func type 
	int lastDeclArgsSize; // Global variable with the last declared argsSize 
	int lastExpressionListSize;

	AST root;

	void printTables() {
		System.out.print("\n\n");
		System.out.print(st);
		System.out.print("\n\n");
		System.out.print(vt);
		System.out.print("\n\n");
		System.out.print(ft);
		System.out.print("\n\n");
	}

	// Exibe a AST no formato DOT em stderr.
    void printAST() {
    	AST.printDot(root, vt);
    }

	/*------------------------------------------------------------------------------*
	 *	Var checking and declaration.
	 *------------------------------------------------------------------------------*/

	// Checks whether the variable was previously declared
	AST checkVar(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = vt.lookupVar(text);
		if (idx == -1) {
			System.out.printf("SEMANTIC ERROR (%d): variable '%s' was not declared.\n", line, text);
			System.exit(1);
		}
		return new AST(NodeKind.VAR_USE_NODE, idx, vt.getType(idx));
	}

	// Creates a new variable from token
	AST newVar(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = vt.lookupVar(text);
		if (idx != -1) {
			System.out.printf("SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
				line, text, vt.getLine(idx)
			);
			System.exit(1);
		}
		idx = vt.addVar(text, line, lastDeclType);
		return new AST(NodeKind.VAR_DECL_NODE, idx, lastDeclType);
	}

	/*------------------------------------------------------------------------------*
	 *	Function checking and declaration.
	 *------------------------------------------------------------------------------*/

	// Checks whether the function was previously declared
	AST checkFunc(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = ft.lookupFunc(text);
		if (idx == -1) {
			System.out.printf("SEMANTIC ERROR (%d): function '%s' was not declared.\n", line, text);
			System.exit(1);
		}
		return new AST(NodeKind.FUNC_CALL_NODE, idx, ft.getType(idx));
	}

	// Creates a new function from token
	AST newFunc(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = ft.lookupFunc(text);
		if (idx != -1) {
			System.out.printf("SEMANTIC ERROR (%d): function '%s' already declared at line %d.\n",
				line, text, ft.getLine(idx)
			);
			System.exit(1);
		}
		idx = ft.addFunc(text, line, lastDeclFuncType, lastDeclArgsSize);
		return new AST(NodeKind.FUNC_DECL_NODE, idx, lastDeclFuncType);
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
				System.out.printf("SEMANTIC ERROR (%d): function '%s' expected %d arguments but received '%d'.\n",
					line, text, argsSize, lastExpressionListSize
				);
				System.exit(1);
			}
		}
	}

	// Checks if a return statement has a compatible type with the function
	void checkFuncReturnType(int lineNo, Type t) {
		if(t != lastDeclFuncType) {
			System.out.printf(
				"SEMANTIC ERROR (%d): Return statement type incompatible with function type. Expected '%s' but received '%s'.\n",
				lineNo, lastDeclFuncType, t
			);
			System.exit(1);
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
		System.exit(1);
    }

	private void checkUnaryOp(int lineNo, String op, Type t) {
		if (t != Type.INT_TYPE && t != Type.FLOAT32_TYPE) {
			System.out.printf(
				"SEMANTIC ERROR (%d): type '%s' not suported for unary operator '%s'.\n",
				lineNo, t.toString(), op
			);
			System.exit(1);
		}
	}

	private void checkAssign(int lineNo, String op,Type l, Type r) {
        if (l != r) {
			typeError(lineNo, op, l, r);
			System.exit(1);
		} 
    }

	private void checkBoolExpr(int lineNo, String cmd, Type t) {
		if (t != Type.BOOL_TYPE) {
			System.out.printf(
				"SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
				lineNo, cmd, t.toString(), Type.BOOL_TYPE.toString()
			);
			System.exit(1);
		}
	}

	private void checkIndex(int lineNo, Type t) {
		if(t != Type.INT_TYPE) {
			System.out.printf(
				"SEMANTIC ERROR (%d): incompatible type '%s' at array index.\n",
				lineNo, t.toString()
			);
			System.exit(1);
		}
    }

	private void checkCase(int lineNo, Type t1, Type t2) {
		if(t1 != t2) {
			System.out.printf(
				"SEMANTIC ERROR (%d): incompatible types for case, expected '%s' but expression is '%s'.\n",
				lineNo, t1.toString(), t2.toString()
			);
			System.exit(1);
		}
	}

	// ----- Specific for when declaring variables

	private void typeInitError(int lineNo, String varName, Type t1, Type t2) {
    	System.out.printf(
			"SEMANTIC ERROR (%d): incompatible types when declaring variable '%s', var type is '%s' and expression type is '%s'.\n",
			lineNo, varName, t1.toString(), t2.toString()
		);
		System.exit(1);
    }

	private void checkInitAssign(int lineNo, String varName, Type l, Type r) {
        if (l != r ) {
			typeInitError(lineNo, varName, l, r);
			System.exit(1);
		}
    }
    
	private void checkArrayInit(int lineNo, String varName) {
		if(lastDeclArgsSize != lastExpressionListSize) {
			System.out.printf(
				"SEMANTIC ERROR (%d): Array '%s' declared with size %d but initialized with %d arguments.\n",
				lineNo, varName, lastDeclArgsSize, lastExpressionListSize
			);
			System.exit(1);
		}
	}

	/*------------------------------------------------------------------------------*
	 *	Visitor for program rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule program: PACKAGE MAIN import_section? func_section
    @Override
	public AST visitProgram(GoParser.ProgramContext ctx) {
		AST funcSection = visit(ctx.func_section());

		// Creates the root node for the program
		this.root = AST.newSubtree(NodeKind.PROGRAM_NODE, Type.NO_TYPE, funcSection);

		// TODO
		// Since the import is optional we can add a new child if necessary
		// if(ctx.import_section() != null) {
		// 	AST importSection = visit(ctx.import_section());

		// 	this.root.addChild(importSection);
		// }

		return this.root;
	}

	/*------------------------------------------------------------------------------*
	 *	Visitors for var_types rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule var_types: INT
	@Override
	public AST visitIntType(GoParser.IntTypeContext ctx) {
		this.lastDeclType = Type.INT_TYPE;
		return null;
	}

	// Visits the rule var_types: STRING
	@Override
	public AST visitStringType(GoParser.StringTypeContext ctx) {
		this.lastDeclType = Type.STRING_TYPE;
		return null;
	}

	// Visits the rule var_types: BOOL
	@Override
	public AST visitBoolType(GoParser.BoolTypeContext ctx) {
		this.lastDeclType = Type.BOOL_TYPE;
		return null;
	}
	
	// Visits the rule var_types: FLOAT32
	@Override
	public AST visitFloat32Type(GoParser.Float32TypeContext ctx) {
		this.lastDeclType = Type.FLOAT32_TYPE;
		return null;
	}

	/*------------------------------------------------------------------------------*
	 *Visitors for var_declaration and declare_assign rules
	 *------------------------------------------------------------------------------*/

	// Visits the rule var_declaration: VAR IDENTIFIER (var_types | var_types? ASSIGN  expression | array_declaration) SEMI?
	@Override
	public AST visitVar_declaration(GoParser.Var_declarationContext ctx) {	
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
				lastDeclType = visit(ctx.expression()).type;
			}
		}

		Token identifierToken = ctx.IDENTIFIER().getSymbol();

		// Checks if the variable was previously declared
		AST varDecl = newVar(identifierToken);

		boolean hasAssign = ctx.ASSIGN() != null;
		// Checks if the identifier type and expression type match
		if(hasAssign) {
			Type identifierType = checkVar(identifierToken).type;
			AST expression = visit(ctx.expression());

			checkInitAssign(identifierToken.getLine(), identifierToken.getText(), identifierType, expression.type);
			
			varDecl.addChild(expression);
		}

		return varDecl;
	}


	// // Visits the rule declare_assign: IDENTIFIER DECLARE_ASSIGN ( array_init | expression) SEMI?
	// @Override
	// public Type visitDeclare_assign(GoParser.Declare_assignContext ctx) {
	// 	Token identifierToken = ctx.IDENTIFIER().getSymbol();

	// 	// Defines lastDeclType based on expression type or array initialization
	// 	if(ctx.expression() != null) lastDeclType = visit(ctx.expression());
	// 	if(ctx.array_init() != null) {
	// 		visit(ctx.array_init());

	// 		// Checks if the array was initialized with the correct amount of indexes
	// 		checkArrayInit(identifierToken.getLine(), identifierToken.getText());
	// 	}

	// 	// Checks if the variable was previously declared
	// 	newVar(identifierToken);

	// 	return Type.NO_TYPE;
	// }

	// /*------------------------------------------------------------------------------*
	//  *Visitor for array_declaration and array_ags rules
	//  *------------------------------------------------------------------------------*/

	// // Visits the rule array_declaration: L_BRACKET DECIMAL_LIT R_BRACKET var_types
	// @Override
	// public Type visitArray_declaration(GoParser.Array_declarationContext ctx) {
	// 	// Defines the array size
	// 	lastDeclArgsSize = Integer.parseInt(ctx.DECIMAL_LIT().getText());

	// 	// Defines lastDeclType 
	// 	visit(ctx.var_types());

	// 	return Type.NO_TYPE;
	// }

	// // Visits the rule array_init: array_declaration L_CURLY expression_list? R_CURLY
	// @Override
	// public Type visitArray_init(GoParser.Array_initContext ctx) {
	// 	// Recursively visits the rule for error checking
	// 	visit(ctx.array_declaration());

	// 	if(ctx.expression_list() != null) {
	// 		// Recursively visits the rule for error checking
	// 		visit(ctx.expression_list());
	// 	} 

	// 	return Type.NO_TYPE;
	// }

	/*------------------------------------------------------------------------------*
	 *	Visitors for input and output
	 *------------------------------------------------------------------------------*/

	// //  Visits the rule input: INPUT L_PAREN AMPERSAND id R_PAREN
	// @Override
	// public Type visitInput(GoParser.InputContext ctx) {
	// 	// Recursively visits the rule for error checking
	// 	visit(ctx.id());

	// 	return Type.NO_TYPE;
	// }

	// // Visits the rule output: OUTPUT L_PAREN expression_list? R_PAREN
	// @Override
	// public Type visitOutput(GoParser.OutputContext ctx) {
	// 	// Recursively visits the rule for error checking
	// 	visit(ctx.expression_list());

	// 	return Type.NO_TYPE;
	// }

	/*------------------------------------------------------------------------------*
	 *	Visitors for functions related rules
	 *------------------------------------------------------------------------------*/
	
	//  TODOs: handle args declaration inside function scope
	// Visits the rule func_declaration: FUNC IDENTIFIER L_PAREN func_args? R_PAREN var_types? statement_section
	@Override
	public AST visitFunc_declaration(GoParser.Func_declarationContext ctx) {
		
		if(ctx.var_types() != null) {
			// Defines lastDeclType 
			visit(ctx.var_types());
		} else {
			// Function has no return type
			lastDeclType = Type.NO_TYPE;
		}

		// Saves the func type before the lastDeclType is overwritten
		// by the args declaration
		lastDeclFuncType = lastDeclType;

		AST funcArgs = null;
		if(ctx.func_args() != null) {
			// Defines lastDeclArgsSize and adds the args to var table
			funcArgs = visit(ctx.func_args());
		} else {
			// Function has no args
			lastDeclArgsSize = 0;
		}
		
		// Checks if the function was previously declared		
		AST node = newFunc(ctx.IDENTIFIER().getSymbol());

		// Recursively visits rule for error checking
		AST statements = visit(ctx.statement_section());
		
		// Adds the statements and args as function's children
		node.addChild(funcArgs);
		node.addChild(statements);

		return node;
	}

	// Visits the rule func_args: id var_types (COMMA id var_types)*
	@Override
	public AST visitFunc_args(GoParser.Func_argsContext ctx) {
		lastDeclArgsSize = ctx.id().size();

		AST node = AST.newSubtree(NodeKind.FUNC_ARGS_NODE, Type.NO_TYPE);

		// TODO: handle scopes, for now its just adding into the global var table
		// Adds every argument into the var table
		for(int i = 0; i < ctx.id().size(); i++) {
			// Defines lastDeclType
			visit(ctx.var_types(i));

			// Checks if the variable was previously declared
			AST child = newVar(ctx.id(i).IDENTIFIER().getSymbol());
			node.addChild(child);

			// Recursively visits the rule for error checking
			visit(ctx.id(i));
		}

		return node;
	}

	// Visits the rule func_section: func_declaration* func_main func_declaration*
	@Override
	public AST visitFunc_section(GoParser.Func_sectionContext ctx) {
		// Creates the func_list node
		AST node = AST.newSubtree(NodeKind.FUNC_LIST_NODE, Type.NO_TYPE);

		// Checks if there is any other function declaration besides the main function
		if(ctx.func_declaration() != null) {
			// Recursively visits the functions for error checking
			for (GoParser.Func_declarationContext funcDecl : ctx.func_declaration()) {
				AST child = visit(funcDecl);
				node.addChild(child);
			}
		}

		// Visits the main function for error checking
		AST mainFunc = visit(ctx.func_main());

		// Adds the main function as func_section's child
		node.addChild(mainFunc);

		return node;
	}

	// Visits the rule func_main: FUNC MAIN L_PAREN func_args? R_PAREN var_types? statement_section
	@Override
	public AST visitFunc_main(GoParser.Func_mainContext ctx) {
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

		AST funcArgs = null;
		if(ctx.func_args() != null) {
			// Defines lastDeclArgsSize
			funcArgs = visit(ctx.func_args());
		} else {
			// Function has no args
			lastDeclArgsSize = 0;
		}
		
		// Recursively visits rule for error checking
		AST statements = visit(ctx.statement_section());

		Token mainToken = ctx.MAIN().getSymbol();

		// Adds the main function into the functions table
		int idx = ft.addFunc(mainToken.getText(), mainToken.getLine(), lastDeclFuncType, lastDeclArgsSize);		 

		// Creates the node for the main function
		AST mainFunc = new AST(NodeKind.FUNC_MAIN_NODE, idx, lastDeclFuncType);

		// Adds the statements as function's child
		mainFunc.addChild(funcArgs);
		mainFunc.addChild(statements);

		return mainFunc;
	}

	/*------------------------------------------------------------------------------*
	 *	Visitors for statements rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule statement_section: L_CURLY statement* return_statement? R_CURLY
	@Override
	public AST visitStatement_section(GoParser.Statement_sectionContext ctx) {
		AST node = AST.newSubtree(NodeKind.STATEMENT_SECTION_NODE, Type.NO_TYPE);

		// Recursively visits every statement for error checking
		for (GoParser.StatementContext stmt : ctx.statement()) {
			AST child = visit(stmt);
			node.addChild(child);
		}

		if(ctx.return_statement() != null) {
			// Recursively visits the rule for error checking
			AST child = visit(ctx.return_statement());
			node.addChild(child);
		}

		return node;
	}

	// Visits the rule return_statement: RETURN expression SEMI?
	@Override
	public AST visitReturn_statement(GoParser.Return_statementContext ctx) {
		Type expressionType = Type.NO_TYPE;
		AST expression = null;
		
		if(ctx.expression() != null) {
			expression = visit(ctx.expression());
			expressionType = expression.type;
		}

		// Checks if the function return type and expression type match
		checkFuncReturnType(ctx.RETURN().getSymbol().getLine(), expressionType);
		
		// Creates the return node
		AST node = AST.newSubtree(NodeKind.RETURN_NODE, expressionType);

		node.addChild(expression);

		return node;
	}

	// // Visits the rule if_statement: IF expression statement_section (ELSE statement_section)?
	// @Override
	// public Type visitIf_statement(GoParser.If_statementContext ctx) {
	// 	Type expressionType = visit(ctx.expression());

	// 	// Checks expression to see if it is bool type 
	// 	checkBoolExpr(ctx.IF().getSymbol().getLine(), "if", expressionType);

	// 	// Recursively visits the statement_section from the if block for error checking
	// 	visit(ctx.statement_section(0));

	// 	// Recursively visits the statement_section from the else block for error checking
	// 	if(ctx.ELSE() != null) {
	// 		visit(ctx.statement_section(1));
	// 	} 

	// 	return Type.NO_TYPE;
	// }

	// // Visits the rule for_statement: FOR expression? statement_section
	// @Override
	// public Type visitWhile(GoParser.WhileContext ctx) {
	// 	// Checks if there is a expression
	// 	if(ctx.expression() != null) {
	// 		Type expressionType = visit(ctx.expression());
	
	// 		// Checks if the expression has bool type
	// 		checkBoolExpr(ctx.FOR().getSymbol().getLine(), "for", expressionType);
	// 	}

	// 	// Recursively visits the statement_section for error checking
	// 	visit(ctx.statement_section());

	// 	return Type.NO_TYPE;
	// }

	// // Visits the rule or_statement: FOR declare_assign SEMI expression SEMI assign_statement statement_section
	// @Override
	// public Type visitFor(GoParser.ForContext ctx) {
	// 	// Recursively visits rule for error checking
	// 	visit(ctx.declare_assign());

	// 	Type expressionType = visit(ctx.expression());

	// 	// Checks if the expression has bool type
	// 	checkBoolExpr(ctx.FOR().getSymbol().getLine(), "for", expressionType);

	// 	// Recursively visits rules for error checking 
	// 	visit(ctx.assign_statement());
	// 	visit(ctx.statement_section());

	// 	return Type.NO_TYPE;
	// }

	// // Visits the rule assign_statement: id op=(ASSIGN | MINUS_ASSIGN | PLUS_ASSIGN) expression SEMI?
	// @Override
	// public Type visitAssignExpression(GoParser.AssignExpressionContext ctx) {
	// 	Type expressionType = visit(ctx.expression());
		
	// 	// Checks if the variable was previously declared
	// 	Type identifierType = visit(ctx.id());
		
	// 	Token identifierToken = ctx.id().IDENTIFIER().getSymbol();

	// 	// Checks if the assign operation is suported
	// 	checkAssign(identifierToken.getLine(), ctx.op.getText(), identifierType, expressionType);

	// 	return Type.NO_TYPE;
	// }

	// // Visits the rule assign_statement: IDENTIFIER op=(PLUS_PLUS | MINUS_MINUS) SEMI?
	// @Override
	// public Type visitAssignPPMM(GoParser.AssignPPMMContext ctx) {
	// 	// Checks if the variable was previously declared
	// 	Type identifierType = visit(ctx.id());

	// 	Token identifierToken = ctx.id().IDENTIFIER().getSymbol();

	// 	// Checks if the operation is suported
	// 	checkUnaryOp(identifierToken.getLine(), ctx.op.getText(), identifierType);
		
	// 	// Since the assignment wont change the var type, there is no need 
	// 	// to call the checkAssign function
	// 	return Type.NO_TYPE;
	// }

	// // Visits the rule switch_statement: SWITCH id? L_CURLY case_statement R_CURLY
	// @Override
	// public Type visitSwitch_statement(GoParser.Switch_statementContext ctx) {
	// 	// Checks if there is a identifier or func call to be evaluated
	// 	// and recursively visits the rule for error checking
	// 	if(ctx.id() != null) visit(ctx.id());	
	// 	if(ctx.func_call() != null) visit(ctx.func_call());	

	// 	// Recursively visits the case_statement for error checking
	// 	visit(ctx.case_statement());

	// 	return Type.NO_TYPE;
	// }

	// // Visits the rule case_statement: (CASE expression COLON statement*)* (DEFAULT COLON statement*)?
	// @Override
	// public Type visitCase_statement(GoParser.Case_statementContext ctx) {
	// 	// Get the parent node of the rule, in this case, the switch node
	// 	GoParser.Switch_statementContext parent = (GoParser.Switch_statementContext) ctx.parent;
		
	// 	// Dont think thats gonna happen, but there it is
	// 	if(parent == null) {
	// 		System.out.println("Case statement has no parent node. Exiting...");
	// 		System.exit(1);
	// 	}

	// 	// Default type for the case expression if nothing is being evaluated
	// 	Type caseType = Type.BOOL_TYPE;

	// 	// Checks if there is a identifier or func_call to be evaluated
	// 	// and set the case type to the same type as the identifier
	// 	if(parent.id() != null) caseType = visit(parent.id());	
	// 	if(parent.func_call() != null) caseType = visit(parent.func_call());	

	// 	// Recursively visits every expression for each case to handle errors
	// 	for(int i = 0; i < ctx.expression().size(); i++) {
	// 		// Get the current expression type
	// 		Type expressionType = visit(ctx.expression(i));
			
	// 		// Check if the expression type matches with the case type
	// 		checkCase(ctx.CASE().get(i).getSymbol().getLine() , caseType, expressionType);
	// 	}

	// 	// TODO: that will probably cause some problems when trying to figure out
	// 	// which statement is from which case
	// 	// Recursively visits every statement for error checking
	// 	for(GoParser.StatementContext stmt : ctx.statement()) {
	// 		visit(stmt);
	// 	}

	// 	// Recursively visits rule for error checking
	// 	if(ctx.default_statement() != null) { 
	// 		visit(ctx.default_statement());
	// 	}

	// 	return Type.NO_TYPE;
	// }
	
	//  Visits the rule func_call: IDENTIFIER L_PAREN expression_list? R_PAREN 
	@Override
	public AST visitFunc_call(GoParser.Func_callContext ctx) {
		Token funcToken = ctx.IDENTIFIER().getSymbol();

		// Checks if the function was previously declared
		AST node = checkFunc(funcToken);

		// Checks if the function call has any parameters
		if(ctx.expression_list() != null) {
			// Recursively visits rule for error checking
			AST child = visit(ctx.expression_list());
			node.addChild(child);
		} else {
			lastExpressionListSize = 0;
		}

		// Checks if the expressionListSize is the same size as what the function expects
		checkFuncCall(funcToken);

		return node;
	}


	// Visits the rule expression_list: expression (COMMA expression)*
	@Override
	public AST visitExpression_list(GoParser.Expression_listContext ctx) {
		lastExpressionListSize = ctx.expression().size();

		AST node = AST.newSubtree(NodeKind.EXPRESSION_LIST_NODE, Type.NO_TYPE);

		// Recursively visits each expression for error checking
		for(GoParser.ExpressionContext expr : ctx.expression()) {
			AST child = visit(expr);
			node.addChild(child);
		}

		return node;
	}


	/*------------------------------------------------------------------------------*
	 *	Visitors for expression rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule expression: expression op=(STAR | DIV | MOD) expression
	@Override
	public AST visitStarDivMod(GoParser.StarDivModContext ctx) {
		// Visits both operands to check their types
		AST l = visit(ctx.expression(0));
		AST r = visit(ctx.expression(1));

		// Unify the types from both operands
		Type lt = l.type;
		Type rt = r.type;
		Type unif = lt.unifyMathOps(rt);

		// Operation not allowed
		if (unif == Type.NO_TYPE) {
			typeError(ctx.op.getLine(), ctx.op.getText(), lt, rt);
		}
		
		// Defines which node kind the expression has
		NodeKind kind = null;
		switch (ctx.op.getType()) {
			case GoParser.STAR:
				kind = NodeKind.STAR_NODE;
				break;
			case GoParser.DIV:
				kind = NodeKind.DIV_NODE;
				break;
			case GoParser.MOD:
				kind = NodeKind.MOD_NODE;
				break;
		}

		return AST.newSubtree(kind, unif, l, r);
	}

	// Visits the rule expression: expression op=(PLUS | MINUS) expression
	@Override
	public AST visitPlusMinus(GoParser.PlusMinusContext ctx) {
		// Visits both operands to check their types
		AST l = visit(ctx.expression(0));
		AST r = visit(ctx.expression(1));
		
		// Unify the types from both operands
		Type lt = l.type;
		Type rt = r.type;
		Type unif = lt.unifyMathOps(rt);

		// Operation not suported
		if (unif == Type.NO_TYPE) {
			typeError(ctx.op.getLine(), ctx.op.getText(), lt, rt);
		}

		if (ctx.op.getType() == GoParser.PLUS) {
			return AST.newSubtree(NodeKind.PLUS_NODE, unif, l, r);
		} else { // MINUS
			return AST.newSubtree(NodeKind.MINUS_NODE, unif, l, r);
		}
	}

	// Visits the rule expression: expression op=( EQUALS | NOT_EQUALS | LESS | LESS_OR_EQUALS | GREATER | GREATER_OR_EQUALS) expression
	@Override
	public AST visitRelationalOperators(GoParser.RelationalOperatorsContext ctx) {
		// Visits both operands to check their types
		AST l = visit(ctx.expression(0));
		AST r = visit(ctx.expression(1));
		
		// Unify the types from both operands
		Type lt = l.type;
		Type rt = r.type;
		Type unif;

		int op = ctx.op.getType();
		if(op == GoParser.EQUALS || op == GoParser.NOT_EQUALS){
			unif = lt.unifyCompare(rt);
		} else {
			unif = lt.unifyCompare2(rt);
		}

		// Operation not suported
		if (unif == Type.NO_TYPE) {
			typeError(ctx.op.getLine(), ctx.op.getText(), lt, rt);
		}

		// Defines which node kind the expression has
		NodeKind kind = null;
		if (op == GoParser.EQUALS) 				kind = NodeKind.EQUALS_NODE;
		if (op == GoParser.NOT_EQUALS) 			kind = NodeKind.NOT_EQUALS_NODE;
		if (op == GoParser.LESS) 				kind = NodeKind.LESS_NODE;
		if (op == GoParser.LESS_OR_EQUALS) 		kind = NodeKind.LESS_OR_EQUALS_NODE;
		if (op == GoParser.GREATER) 			kind = NodeKind.GREATER_NODE;
		if (op == GoParser.GREATER_OR_EQUALS) 	kind = NodeKind.GREATER_OR_EQUALS_NODE;

		return AST.newSubtree(kind, unif, l, r);
	}

	// Visits the rule expression: L_PAREN expression R_PAREN 
	@Override
	public AST visitExpressionParen(GoParser.ExpressionParenContext ctx) {
		return visit(ctx.expression());
	}

	// Visits the rule expression: id
	@Override
	public AST visitExpressionId(GoParser.ExpressionIdContext ctx) {
		return visit(ctx.id());
	}

	// Visits the rule expression: func_call
	@Override
	public AST visitExpressionFuncCall(GoParser.ExpressionFuncCallContext ctx) {
		return visit(ctx.func_call());
	}

	// Visits the rule expression: DECIMAL_LIT
	@Override
	public AST visitIntVal(GoParser.IntValContext ctx) {
		int intData = Integer.parseInt(ctx.getText());
		return new AST(NodeKind.INT_VAL_NODE, intData, Type.INT_TYPE);
	}

	// Visits the rule expression: FLOAT_LIT
	@Override
	public AST visitFloatVal(GoParser.FloatValContext ctx) {
		float floatData = Float.parseFloat(ctx.getText());
		return new AST(NodeKind.FLOAT32_VAL_NODE, floatData, Type.FLOAT32_TYPE);
	}

	// Visits the rule expression: INTERPRETED_STRING_LIT
	@Override
	public AST visitStringVal(GoParser.StringValContext ctx) {
		// Adds the string to the string table.
		int idx = st.addString(ctx.INTERPRETED_STRING_LIT().getText());

		return new AST(NodeKind.STRING_VAL_NODE, idx, Type.STRING_TYPE);
	}

	// Visits the rule expression: BOOLEAN_LIT
	@Override
	public AST visitBoolVal(GoParser.BoolValContext ctx) {
		System.out.println(ctx.getText());
		if(ctx.getText() == "true") {
			return new AST(NodeKind.BOOL_VAL_NODE, 1, Type.BOOL_TYPE);
		} else {
			return new AST(NodeKind.BOOL_VAL_NODE, 0, Type.BOOL_TYPE);
		}
	}

	/*------------------------------------------------------------------------------*
	 *	Visitor for id rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule id: IDENTIFIER (L_BRACKET expression R_BRACKET)?
	@Override
	public AST visitId(GoParser.IdContext ctx) {
		Token identifierToken = ctx.IDENTIFIER().getSymbol();

		// Checks if it has an array index 
		if(ctx.expression() != null) {
			// Recursively visits the expression for error checking
			AST expression = visit(ctx.expression());
	
			// Checks if the index is valid
			checkIndex(identifierToken.getLine(), expression.type);
		}

		return checkVar(identifierToken);
	}

}
