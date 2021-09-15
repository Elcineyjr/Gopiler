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
	 *	Input / Output
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitInput(AST node) {
		int varIdx = node.getChild(0).intData;
		Type varType = vt.getType(varIdx);

		switch(varType) {
			case INT_TYPE:  		readInt(varIdx);    	break;
	        case FLOAT32_TYPE: 		readReal(varIdx);   	break;
			case BOOL_TYPE: 		readBool(varIdx);   	break;
			case STRING_TYPE:  		readString(varIdx); 	break;
			case NO_TYPE:
		    default:
	            System.err.printf("Invalid type: %s!\n", varType.toString());
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

	private Void readReal(int varIdx) {
		System.out.printf("read (real): ");
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
		String s = in.next();
		int strIdx = st.addString(s);
		memory.storeInt(varIdx, strIdx);
		return null;
	}

	@Override
	protected Void visitOutput(AST node) {
		return null; 
	}

	/*------------------------------------------------------------------------------*
	 *	Relational operations
	 *------------------------------------------------------------------------------*/

	@Override
	protected Void visitEquals(AST node) {
		return null; 
	}

	@Override
	protected Void visitNotEquals(AST node) {
		return null; 
	}

	@Override
	protected Void visitLess(AST node) {
		return null; 
	}

	@Override
	protected Void visitLessOrEquals(AST node) {
		return null; 
	}

	@Override
	protected Void visitGreater(AST node) {
		return null; 
	}

	@Override
	protected Void visitGreaterOrEquals(AST node) {
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
		// Visits all children from statement section
		for (int i = 0; i < node.getChildCount(); i++) {
			visit(node.getChild(i));
		}

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

	@Override
	protected Void visitFuncMain(AST node) {
		// Visits the main function's statement section
		visit(node.getChild(0));

		return null; 
	}

	@Override
	protected Void visitFuncDecl(AST node) {
		// Visits the function's statement section
		visit(node.getChild(0));

		return null; 
	}

	@Override
	protected Void visitFuncArgs(AST node) {
		return null; 
	}

	@Override
	protected Void visitExpressionList(AST node) {
		return null; 
	}

	@Override
	protected Void visitProgram(AST node) {
		// Visits the function list node
		visit(node.getChild(0));

		return null; 
	}

	@Override
	protected Void visitFuncList(AST node) {
		int funcListSize = node.getChildCount() - 1;

		// Visits all func declaration nodes
		for (int i = 0; i < funcListSize; i++) {
			visit(node.getChild(i));
		}

		// Not necessary to keep it separated but for explicity sake
		// Visits the main function declarion
		visit(node.getChild(funcListSize));

		return null; 
	}

	@Override
	protected Void visitVarUse(AST node) {
		return null; 
	}

}
