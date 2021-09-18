package code;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Scanner;

import ast.AST;
import ast.ASTBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import typing.Type;

public class Interpreter extends ASTBaseVisitor<Void> {

	private final DataStack stack;
	private final Memory memory;
	private final StrTable st;
	private final VarTable vt;
	private final Scanner in;

	public Interpreter(StrTable st, VarTable vt) {
		this.stack = new DataStack();
		this.memory = new Memory(vt);
		this.st = st;
		this.vt = vt;
		this.in = new Scanner(System.in);
	}

	// Helper method that visits every child from a given node
	// to avoid code repetition
	void visitsEveryChild(AST node) {
		for (AST child : node.getChildren()) {
			visit(child);
		}
	}


	/*------------------------------------------------------------------------------*
	 *	Var values
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitBoolVal(AST node) {
		// recursion base
		stack.pushInt(node.intData);
		return null; 
	}

	@Override
	protected Void visitIntVal(AST node) {
		// recursion base
		stack.pushInt(node.intData);
		return null; 
	}

	@Override
	protected Void visitFloatVal(AST node) {
		// recursion base
		stack.pushFloat(node.floatData);
		return null; 
	}

	@Override
	protected Void visitStringVal(AST node) {
		// recursion base
		stack.pushInt(node.intData);
		return null; 
	}

	/*------------------------------------------------------------------------------*
	 *	Input
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitInput(AST node) {
		int varIdx = node.getChild(0).intData;
		Type varType = vt.getType(varIdx);

		switch(varType) {
			case INT_TYPE:  		readInt(varIdx);    	break;
	        case FLOAT32_TYPE: 		readFloat32(varIdx);   	break;
			case BOOL_TYPE: 		readBool(varIdx);   	break;
			case STRING_TYPE:  		readString(varIdx); 	break;
			case NO_TYPE:
		    default:
	            System.err.printf("Invalid input type: %s!\n", varType.toString());
	            System.exit(1);
		}
		return null;
	}

	private Void readInt(int varIdx) {
		System.out.printf("read (int): ");
		int value = in.nextInt();
		memory.storeInt(varIdx, value);
		return null; 
	}

	private Void readFloat32(int varIdx) {
		System.out.printf("read (float32): ");
		float value = in.nextFloat();
		memory.storeFloat(varIdx, value);
		return null;
	}

	private Void readBool(int varIdx) {
		int value;
	    do {
	        System.out.printf("read (bool - 0 = false, 1 = true): ");
	        value = in.nextInt();
	    } while (value != 0 && value != 1);
	    memory.storeInt(varIdx, value);
	    return null;
	}

	private Void readString(int varIdx) {
		System.out.printf("read (str): ");

		// Changes the default delimiter to read senteces with spaces
		// then resets it
		in.useDelimiter("\n");
		String s = in.next();
		in.reset();

		int strIdx = st.addString(s);
		memory.storeInt(varIdx, strIdx);
		return null;
	}

	/*------------------------------------------------------------------------------*
	 *	Output
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitOutput(AST node) {
		// Get the expression list node
		AST expressionList = node.getChild(0);

		// Iterates over the expression list's children to print them
		for (AST expression : expressionList.getChildren()) {
			visit(expression);
			
			switch(expression.type) {
				case INT_TYPE:  	writeInt();			break;
				case FLOAT32_TYPE: 	writeFloat32();		break;
				case BOOL_TYPE: 	writeBool();   		break;
				case STRING_TYPE:  	writeString();		break;
				case NO_TYPE:
				default:
					System.err.printf("Invalid output type: %s!\n", expression.type.toString());
					System.exit(1);
			}
		}

		return null;
	}

	private Void writeInt() {
		System.out.println(stack.popInt());
		return null;
	}

	private Void writeFloat32() {
		System.out.println(stack.popFloat());
		return null;
	}

	private Void writeBool() {
		if (stack.popInt() == 0) {
			System.out.println("false");
		} else {
			System.out.println("true");
		}
		return null;
	}

	private Void writeString() {
		int stringIdx = stack.popInt();
		String originalString = st.get(stringIdx);
		// String unescapedStr = unescapeStr(originalStr);
		// System.out.print(unescapedStr);
		System.out.println(originalString);
		return null;
	}

	/*------------------------------------------------------------------------------*
	 *	Relational operations
	 *------------------------------------------------------------------------------*/

	private void execRelationalComparison(Type t, String op) {
		// Required to initialize, but the if clauses will overwrite the value
		boolean result = false;

		// Compares int and boolean (represented as int) values 
		if(t == Type.INT_TYPE || t == Type.BOOL_TYPE) {
			int r = stack.popInt();
			int l = stack.popInt();

			// Some of these operations are not valid for BOOL_TYPE, 
			// but the semantic checker handles it
			switch (op) {
				case "==":		result = l == r;
				case "!=":		result = l != r;
				case "<":		result = l < r;
				case "<=":		result = l <= r;
				case ">":		result = l > r;
				case ">=":		result = l >= r;
			}
		}

		// Compares strings
		if(t == Type.STRING_TYPE) {
			String rString = st.get(stack.popInt());
			String lString = st.get(stack.popInt());

			switch (op) {
				case "==":		result = lString.compareTo(rString) == 0;
				case "!=":		result = lString.compareTo(rString) != 0;
				case "<":		result = lString.compareTo(rString) < 0;
				case "<=":		result = lString.compareTo(rString) <= 0;
				case ">":		result = lString.compareTo(rString) > 0;
				case ">=":		result = lString.compareTo(rString) >= 0;
			}
		}

		// Compares float values
		if(t == Type.FLOAT32_TYPE) {
			float r = stack.popFloat();
			float l = stack.popFloat();

			switch (op) {
				case "==":		result = l == r;
				case "!=":		result = l != r;
				case "<":		result = l < r;
				case "<=":		result = l <= r;
				case ">":		result = l > r;
				case ">=":		result = l >= r;
			}
		}

		if (result) {
			stack.pushInt(1); // true
		} else {
			stack.pushInt(0); // false
		}

	}

	@Override
	protected Void visitEquals(AST node) {
		AST lNode = node.getChild(0);
		AST rNode = node.getChild(1);

		visit(lNode);
		visit(rNode);

		execRelationalComparison(lNode.type, "==");

		return null; 
	}

	@Override
	protected Void visitNotEquals(AST node) {
		AST lNode = node.getChild(0);
		AST rNode = node.getChild(1);

		visit(lNode);
		visit(rNode);

		execRelationalComparison(lNode.type, "!=");

		return null;
	}

	@Override
	protected Void visitLess(AST node) {
		AST lNode = node.getChild(0);
		AST rNode = node.getChild(1);

		visit(lNode);
		visit(rNode);

		execRelationalComparison(lNode.type, "<");

		return null;
	}

	@Override
	protected Void visitLessOrEquals(AST node) {
		AST lNode = node.getChild(0);
		AST rNode = node.getChild(1);

		visit(lNode);
		visit(rNode);

		execRelationalComparison(lNode.type, "<=");

		return null; 
	}

	@Override
	protected Void visitGreater(AST node) {
		AST lNode = node.getChild(0);
		AST rNode = node.getChild(1);

		visit(lNode);
		visit(rNode);

		execRelationalComparison(lNode.type, ">");

		return null;  
	}

	@Override
	protected Void visitGreaterOrEquals(AST node) {
		AST lNode = node.getChild(0);
		AST rNode = node.getChild(1);

		visit(lNode);
		visit(rNode);

		execRelationalComparison(lNode.type, ">=");

		return null; 
	}

	/*------------------------------------------------------------------------------*
	 *	Arithmetics operations
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitStar(AST node) {
		return null; 
	}

	@Override
	protected Void visitDiv(AST node) {
		return null; 
	}

	@Override
	protected Void visitMod(AST node) {
		return null; 
	}

	@Override
	protected Void visitPlus(AST node) {
		return null; 
	}

	@Override
	protected Void visitMinus(AST node) {
		return null; 
	}


	/*------------------------------------------------------------------------------*
	 *	Statements
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitStatementSection(AST node) {
		visitsEveryChild(node);
		return null; 
	}

	@Override
	protected Void visitReturn(AST node) {
		return null; 
	}

	@Override
	protected Void visitVarDecl(AST node) {
		return null; 
	}

	@Override
	protected Void visitDeclareAssign(AST node) {
		return null; 
	}

	@Override
	protected Void visitAssign(AST node) {
		return null; 
	}

	@Override
	protected Void visitPlusAssign(AST node) {
		return null; 
	}

	@Override
	protected Void visitMinusAssign(AST node) {
		return null; 
	}

	@Override
	protected Void visitPlusPlus(AST node) {
		return null; 
	}

	@Override
	protected Void visitMinusMinus(AST node) {
		return null; 
	}

	@Override
	protected Void visitIf(AST node) {
		return null; 
	}

	@Override
	protected Void visitElse(AST node) {
		return null; 
	}

	@Override
	protected Void visitWhile(AST node) {
		return null; 
	}

	@Override
	protected Void visitFor(AST node) {
		return null; 
	}
	
	@Override
	protected Void visitSwitch(AST node) {
		return null; 
	}

	@Override
	protected Void visitCase(AST node) {
		return null; 
	}

	@Override
	protected Void visitDefault(AST node) {
		return null; 
	}

	@Override
	protected Void visitFuncCall(AST node) {
		return null; 
	}

	/*------------------------------------------------------------------------------*
	 *	Functions
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitFuncMain(AST node) {
		// Visits the main function's statement section
		visit(node.getChild(0));

		return null; 
	}

	@Override
	protected Void visitFuncDecl(AST node) {
		visitsEveryChild(node);
		return null; 
	}

	@Override
	protected Void visitFuncArgs(AST node) {
		visitsEveryChild(node);
		return null; 
	}

	/*------------------------------------------------------------------------------*
	 *	Others
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitExpressionList(AST node) {
		visitsEveryChild(node);
		return null; 
	}

	@Override
	protected Void visitProgram(AST node) {
		// Visits the function list node
		visit(node.getChild(0));

		// End of program, no need to read from stdin anymore
		in.close();

		return null; 
	}

	@Override
	protected Void visitFuncList(AST node) {
		visitsEveryChild(node);
		return null; 
	}

	@Override
	protected Void visitVarUse(AST node) {
		int varIdx = node.intData;
		if (node.type == Type.FLOAT32_TYPE) {
			stack.pushFloat(memory.loadFloat(varIdx));
		} else {
			stack.pushInt(memory.loadInt(varIdx));
		}
		return null; 
	}

}
