package checker;

import org.antlr.v4.runtime.Token;

import parser.GoParser;
import parser.GoParserBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import typing.Type;


public class SemanticChecker extends GoParserBaseVisitor<Type> {

	private StrTable st = new StrTable(); // Tabela de strings.
	private VarTable vt = new VarTable(); // Tabela de variáveis.

	Type lastDeclType; // Variável "global" com o último tipo declarado.

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
			System.err.printf("SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n", line, text, vt.getLine(idx));
			passed = false;
			return;
		}
		vt.addVar(text, line, lastDeclType);
	}

    /*------------------------------------------------------------------------------*
	 *	Type checking and inference.
	 *------------------------------------------------------------------------------*/
	
    private void typeError(int lineNo, String op, Type t1, Type t2) {
    	System.out.printf(
			"SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
			lineNo, op, t1.toString(), t2.toString()
		);
    	passed = false;
    }

	private void checkAssign(int lineNo, Type l, Type r) {
        if (l == Type.BOOL_TYPE && r != Type.BOOL_TYPE) typeError(lineNo, "=", l, r);
        if (l == Type.STRING_TYPE  && r != Type.STRING_TYPE)  typeError(lineNo, "=", l, r);
        if (l == Type.INT_TYPE  && r != Type.INT_TYPE)  typeError(lineNo, "=", l, r);
        if (l == Type.FLOAT32_TYPE && !(r == Type.INT_TYPE || r == Type.FLOAT32_TYPE)) typeError(lineNo, "=", l, r);
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
	 *Visitors for var_declaration rule
	 *------------------------------------------------------------------------------*/

	// Visits the rule var_declaration: VAR IDENTIFIER (var_types | var_types? ASSIGN  expression | array_declaration) SEMI?
	@Override
	public Type visitVarDeclaration(GoParser.VarDeclarationContext ctx) {
		boolean hasVarType = ctx.var_types() != null;
		boolean hasAssign = ctx.ASSIGN() != null;

		// TODO: array declarations
		if(!hasVarType && !hasAssign) {
			visit(ctx.array_declaration());

			return Type.NO_TYPE;
		} 

		// TODO: Refactor to remove unnecessary lines

		// Var type must be inferred based on expression
		// e.g: var x = true
		if(!hasVarType && hasAssign) {
			// Defines lastDeclType based on expression type
			lastDeclType = visit(ctx.expression());

			// Checks if the variable was previously declared
			newVar(ctx.IDENTIFIER().getSymbol());

			return Type.NO_TYPE;
		} 
		
		// var must be zero-valued according with var type
		// e.g: var y int 
		if(hasVarType && !hasAssign) {
			visit(ctx.var_types()); // defines lastDeclType

			// Checks if the variable was previously declared
			newVar(ctx.IDENTIFIER().getSymbol());
			
			return Type.NO_TYPE;
		}

		// var must be declared and assigned
		// e.g: var z string = "Hello" 
		if(hasVarType && hasAssign) {
			visit(ctx.var_types()); // defines lastDeclType

			Token identifierToken = ctx.IDENTIFIER().getSymbol();

			// Checks if the variable was previously declared
			newVar(identifierToken);

			// Get the declared variable
			Type identifierType = checkVar(identifierToken);
			
			// Checks if the identifier type and expression type match
			checkInitAssign(identifierToken.getLine(), identifierToken.getText(), identifierType, lastDeclType);

			return Type.NO_TYPE;
		}

		return Type.NO_TYPE;
	}


	// Visits the rule var_declaration: IDENTIFIER DECLARE_ASSIGN ( array_declaration | expression) SEMI?
	@Override
	public Type visitDeclareAssign(GoParser.DeclareAssignContext ctx) {
		// Defines lastDeclType based on expression type
		lastDeclType = visit(ctx.expression());

		// Checks if the variable was previously declared
		newVar(ctx.IDENTIFIER().getSymbol());

		return Type.NO_TYPE;
	}


	/*------------------------------------------------------------------------------*
	 *	Visitors for expression rule
	 *------------------------------------------------------------------------------*/

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

}
