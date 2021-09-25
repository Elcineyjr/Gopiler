package code;

import ast.AST;
import ast.ASTBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import typing.Type;

import static code.OpCode.*;
import static code.Instruction.INSTR_MEM_SIZE;

public final class CodeGen extends ASTBaseVisitor<Integer> {
    private final Instruction code[]; // Code memory
	private final StrTable st;
	private final VarTable vt;

	private static int nextInstr;
	private static int intRegsCount;
	private static int floatRegsCount;
	
	public CodeGen(StrTable st, VarTable vt) {
		this.code = new Instruction[INSTR_MEM_SIZE];
		this.st = st;
		this.vt = vt;
	}
	
	@Override
	public void execute(AST root) {
		nextInstr = 0;
		intRegsCount = 0;
		floatRegsCount = 0;
	    dumpStrTable();
	    visit(root);
	    emit(HALT);
	    dumpProgram();
	}

    /*------------------------------------------------------------------------------*
	 *	Prints
	 *------------------------------------------------------------------------------*/

	void dumpProgram() {
	    for (int addr = 0; addr < nextInstr; addr++) {
	    	System.out.printf("%s\n", code[addr].toString());
	    }
	}

	void dumpStrTable() {
	    for (int i = 0; i < st.size(); i++) {
	        System.out.printf("SSTR %s\n", st.get(i));
	    }
	}
	
    /*------------------------------------------------------------------------------*
	 *	Emits
	 *------------------------------------------------------------------------------*/
	
	private void emit(OpCode op, int o1, int o2, int o3) {
		Instruction instr = new Instruction(op, o1, o2, o3);
	    code[nextInstr] = instr;
	    nextInstr++;
	}
	
	private void emit(OpCode op) {
		emit(op, 0, 0, 0);
	}
	
	private void emit(OpCode op, int o1) {
		emit(op, o1, 0, 0);
	}
	
	private void emit(OpCode op, int o1, int o2) {
		emit(op, o1, o2, 0);
	}

	private void backpatchJump(int instrAddr, int jumpAddr) {
	    code[instrAddr].o1 = jumpAddr;
	}

	private void backpatchBranch(int instrAddr, int offset) {
	    code[instrAddr].o2 = offset;
	}

    /*------------------------------------------------------------------------------*
	 *	Registers
	 *------------------------------------------------------------------------------*/
	
	private int newIntReg() {
		return intRegsCount++; 
	}
    
	private int newFloatReg() {
		return floatRegsCount++;
	}

    /*------------------------------------------------------------------------------*
	 *	Values
	 *------------------------------------------------------------------------------*/

    @Override
	protected Integer visitBoolVal(AST node) {
		int x = newIntReg();
	    int c = node.intData;

		// Emits the load immediate with the bool data
	    emit(LDIi, x, c);

	    return x;
	}

	@Override
	protected Integer visitIntVal(AST node) {
		int x = newIntReg();
	    int c = node.intData;

		// Emits the load immediate with the int data
	    emit(LDIi, x, c);

	    return x;
	}

	@Override
	protected Integer visitFloatVal(AST node) {
		int x = newFloatReg();
	    // We need to read as an int because the NSTM cannot handle floats directly.
	    // But we have a float stored in the AST, so we just convert it as an int
	    // and magically we have a float encoded as an int... :P
	    int c = Float.floatToIntBits(node.floatData);

		// Emits the load immediate with the float(as int, see above) data
	    emit(LDIf, x, c);

	    return x;
	}

	@Override
	protected Integer visitStringVal(AST node) {
		int x = newIntReg();
	    int c = node.intData;

		// Emits the load immediate using the string index
	    emit(LDIi, x, c);

	    return x;
	}

	/*------------------------------------------------------------------------------*
	 *	Input
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitInput(AST node) {
		AST var = node.getChild(0);
	    int addr = var.intData;
	    int x;

		// Creates a new register, emits the read call and then emits the store word call
		switch (var.type) {
			case INT_TYPE:
				x = newIntReg();
				emit(CALL, 0, x); 		// read int call
				emit(STWi, addr, x);	// store int call
				break;
			case FLOAT32_TYPE:
				x = newFloatReg();
				emit(CALL, 1, x);		// read float call
				emit(STWf, addr, x);	// store float call
				break;
			case BOOL_TYPE:
				x = newIntReg();
				emit(CALL, 2, x);		// read bool call
				emit(STWi, addr, x);	// store bool call
				break;
			case STRING_TYPE:
				x = newIntReg();
				emit(CALL, 3, x);		// read string call
				emit(STWi, addr, x);	// store string call
				break;
			default:
				System.err.printf("Invalid type: %s!\n", var.type.toString());
				System.exit(1);
		}

	    return null;
	}

	/*------------------------------------------------------------------------------*
	 *	Output
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitOutput(AST node) {
		// Get the expression list node
		AST expressionList = node.getChild(0);
		
		// Iterates over the expression list's children to emit the print call
		for (AST expression : expressionList.getChildren()) {
			int x = visit(expression);

			switch(expression.type) {
				case INT_TYPE:  	emit(CALL, 4, x);  break;
				case FLOAT32_TYPE: 	emit(CALL, 5, x);  break;
				case BOOL_TYPE: 	emit(CALL, 6, x);  break;
				case STRING_TYPE:  	emit(CALL, 7, x);  break;
				case NO_TYPE:
				default:
					System.err.printf("Invalid type: %s!\n", expression.type.toString());
					System.exit(1);
			}
		}

		return null;
	}

	/*------------------------------------------------------------------------------*
	 *	Relational operations
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitEquals(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);

		// Visits each expression and get their registers
		int y = visit(l);
		int z = visit(r);

		// Result register
		int x = newIntReg();

		// Emits the 'equals operation' for the corresponding type
		switch (l.type) {
			case INT_TYPE:			emit(EQUi, x, y, z);	break;
			case FLOAT32_TYPE:		emit(EQUf, x, y, z);	break;
			case BOOL_TYPE:			emit(EQUi, x, y, z);	break;
			case STRING_TYPE:		emit(EQUs, x, y, z);	break;
			default:
				System.err.printf("Invalid type: %s!\n", l.type.toString());
				System.exit(1);
		}

	    return x;
	}

	@Override
	protected Integer visitNotEquals(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);

		// Visits each expression and get their registers
		int y = visit(l);
		int z = visit(r);

		// Result register
		int x = newIntReg();

		// Emits the 'not equals' operation for the corresponding type
		switch (l.type) {
			case INT_TYPE:			emit(NEQi, x, y, z);	break;
			case FLOAT32_TYPE:		emit(NEQf, x, y, z);	break;
			case BOOL_TYPE:			emit(NEQi, x, y, z);	break;
			case STRING_TYPE:		emit(NEQs, x, y, z);	break;
			default:
				System.err.printf("Invalid type: %s!\n", l.type.toString());
				System.exit(1);
		}

	    return x;
	}

	@Override
	protected Integer visitLess(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);

		// Visits each expression and get their registers
		int y = visit(l);
		int z = visit(r);

		// Result register
		int x = newIntReg();

		// Emits the 'less than' for the corresponding type
		switch (l.type) {
			case INT_TYPE:			emit(LTHi, x, y, z);	break;
			case FLOAT32_TYPE:		emit(LTHf, x, y, z);	break;
			case BOOL_TYPE:			emit(LTHi, x, y, z);	break;
			case STRING_TYPE:		emit(LTHs, x, y, z);	break;
			default:
				System.err.printf("Invalid type: %s!\n", l.type.toString());
				System.exit(1);
		}

	    return x;
	}

	@Override
	protected Integer visitLessOrEquals(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);

		// Visits each expression and get their registers
		int y = visit(l);
		int z = visit(r);

		// Result register
		int x = newIntReg();

		// Emits the 'less than or equals to' for the corresponding type
		switch (l.type) {
			case INT_TYPE:			emit(LTEi, x, y, z);	break;
			case FLOAT32_TYPE:		emit(LTEf, x, y, z);	break;
			case BOOL_TYPE:			emit(LTEi, x, y, z);	break;
			case STRING_TYPE:		emit(LTEs, x, y, z);	break;
			default:
				System.err.printf("Invalid type: %s!\n", l.type.toString());
				System.exit(1);
		}

	    return x;
	}

	@Override
	protected Integer visitGreater(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);

		// Visits each expression and get their registers
		int y = visit(l);
		int z = visit(r);

		// Result register
		int x = newIntReg();

		// Emits the 'greater than' for the corresponding type
		switch (l.type) {
			case INT_TYPE:			emit(GTHi, x, y, z);	break;
			case FLOAT32_TYPE:		emit(GTHf, x, y, z);	break;
			case BOOL_TYPE:			emit(GTHi, x, y, z);	break;
			case STRING_TYPE:		emit(GTHs, x, y, z);	break;
			default:
				System.err.printf("Invalid type: %s!\n", l.type.toString());
				System.exit(1);
		}

	    return x;
	}

	@Override
	protected Integer visitGreaterOrEquals(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);

		// Visits each expression and get their registers
		int y = visit(l);
		int z = visit(r);

		// Result register
		int x = newIntReg();

		// Emits the 'greater than or equals to' for the corresponding type
		switch (l.type) {
			case INT_TYPE:			emit(GTEi, x, y, z);	break;
			case FLOAT32_TYPE:		emit(GTEf, x, y, z);	break;
			case BOOL_TYPE:			emit(GTEi, x, y, z);	break;
			case STRING_TYPE:		emit(GTEs, x, y, z);	break;
			default:
				System.err.printf("Invalid type: %s!\n", l.type.toString());
				System.exit(1);
		}

	    return x;
	}

	/*------------------------------------------------------------------------------*
	 *	Arithmetic operations
	 *------------------------------------------------------------------------------*/

	
	@Override
	protected Integer visitStar(AST node) {
		// Visits each expression and get their registers
	    int y = visit(node.getChild(0));
	    int z = visit(node.getChild(1));

		// Result register
		int x;

		// Emits the 'multiply' for the corresponding type
	    if (node.type == Type.FLOAT32_TYPE) {
	        x = newFloatReg();
	        emit(MULf, x, y, z);
	    } else {
	        x = newIntReg();
	        emit(MULi, x, y, z);
	    }

	    return x;
	}

	@Override
	protected Integer visitDiv(AST node) {
		// Visits each expression and get their registers
	    int y = visit(node.getChild(0));
	    int z = visit(node.getChild(1));

		// Result register
		int x;

		// Emits the 'division' for the corresponding type
	    if (node.type == Type.FLOAT32_TYPE) {
	        x = newFloatReg();
	        emit(DIVf, x, y, z);
	    } else {
	        x = newIntReg();
	        emit(DIVi, x, y, z);
	    }

	    return x;
	}

	@Override
	protected Integer visitMod(AST node) {
		// Visits each expression and get their registers
	    int y = visit(node.getChild(0));
	    int z = visit(node.getChild(1));

		// Result register
		int x;

		// Emits the 'modulus' for the corresponding type
	    if (node.type == Type.FLOAT32_TYPE) {
	        x = newFloatReg();
	        emit(MODf, x, y, z);
	    } else {
	        x = newIntReg();
	        emit(MODi, x, y, z);
	    }

	    return x;
	}

	@Override
	protected Integer visitPlus(AST node) {
		// Visits each expression and get their registers
	    int y = visit(node.getChild(0));
	    int z = visit(node.getChild(1));

		// Result register
		int x;

		// Emits the 'addition' for the corresponding type
	    if (node.type == Type.FLOAT32_TYPE) {
	        x = newFloatReg();
	        emit(ADDf, x, y, z);
	    } else {
	        x = newIntReg();
	        emit(ADDi, x, y, z);
	    }

	    return x;
	}

	@Override
	protected Integer visitMinus(AST node) {
		// Visits each expression and get their registers
	    int y = visit(node.getChild(0));
	    int z = visit(node.getChild(1));

		// Result register
		int x;

		// Emits the 'subtraction' for the corresponding type
	    if (node.type == Type.FLOAT32_TYPE) {
	        x = newFloatReg();
	        emit(SUBf, x, y, z);
	    } else {
	        x = newIntReg();
	        emit(SUBi, x, y, z);
	    }

	    return x;
	}


	/*------------------------------------------------------------------------------*
	 *	Statements
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitStatementSection(AST node) {
		// Visits every statement inside block
		for (AST child : node.getChildren()) {
			visit(child);
		}

		return null; 
	}

	@Override
	protected Integer visitReturn(AST node) {
		return null; 
	}

	@Override
	protected Integer visitVarDecl(AST node) {
		return null;
	}

	@Override
	protected Integer visitAssign(AST node) {
		return null;
	}

	@Override
	protected Integer visitPlusAssign(AST node) {
		return null;
	}

	@Override
	protected Integer visitMinusAssign(AST node) {
		return null;
	}

	@Override
	protected Integer visitPlusPlus(AST node) {
		return null;
	}

	@Override
	protected Integer visitMinusMinus(AST node) {
		return null;
	}

	@Override
	protected Integer visitIf(AST node) {
		return null;
	}

	@Override
	protected Integer visitElse(AST node) {
		return null;
	}

	@Override
	protected Integer visitWhile(AST node) {
		return null; 
	}

	@Override
	protected Integer visitFor(AST node) {
		return null; 
	}
	
	@Override
	protected Integer visitSwitch(AST node) {
		return null; 
	}

	@Override
	protected Integer visitCase(AST node) {
		return null; 
	}

	@Override
	protected Integer visitDefault(AST node) {
		return null; 
	}

	@Override
	protected Integer visitFuncCall(AST node) {
		return null;
	}

	/*------------------------------------------------------------------------------*
	 *	Functions
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitFuncMain(AST node) {
		// Visits the statement section node
		visit(node.getChild(0));

		return null;
	}

	@Override
	protected Integer visitFuncDecl(AST node) {
		return null;
	}

	@Override
	protected Integer visitFuncArgs(AST node) {
		return null; 
	}

	/*------------------------------------------------------------------------------*
	 *	Others
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitExpressionList(AST node) {
		return null; 
	}

	@Override
	protected Integer visitProgram(AST node) {
		// Visits the function list node
		visit(node.getChild(0));

		return null;
	}

	@Override
	protected Integer visitFuncList(AST node) {
		// Visits every func declaration
		for (AST child : node.getChildren()) {
			visit(child);
		}

		return null;
	}

	@Override
	protected Integer visitVarUse(AST node) {
		// Get the var index at the var table
		int addr = node.intData;
	    int x;

		// Emits a load from address to a register according with the variable type
	    if (node.type == Type.FLOAT32_TYPE) {
	        x = newFloatReg();
	        emit(LDWf, x, addr);
	    } else {
	        x = newIntReg();
	        emit(LDWi, x, addr);
	    }

	    return x;
	}
}
