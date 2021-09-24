package code;

import ast.AST;
import ast.ASTBaseVisitor;
import tables.StrTable;
import tables.VarTable;

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
	
	// Função principal para geração de código.
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
	 *	AST Traversal
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
		return null;
	}

	@Override
	protected Integer visitIntVal(AST node) {
		return null; 
	}

	@Override
	protected Integer visitFloatVal(AST node) {
		return null; 
	}

	@Override
	protected Integer visitStringVal(AST node) {
		return null; 
	}

	/*------------------------------------------------------------------------------*
	 *	Input
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitInput(AST node) {
		return null;
	}

	/*------------------------------------------------------------------------------*
	 *	Output
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitOutput(AST node) {
		return null;
	}

	/*------------------------------------------------------------------------------*
	 *	Relational operations
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitEquals(AST node) {
		return null; 
	}

	@Override
	protected Integer visitNotEquals(AST node) {
		return null;
	}

	@Override
	protected Integer visitLess(AST node) {
		return null;
	}

	@Override
	protected Integer visitLessOrEquals(AST node) {
		return null; 
	}

	@Override
	protected Integer visitGreater(AST node) {
		return null;  
	}

	@Override
	protected Integer visitGreaterOrEquals(AST node) {
		return null; 
	}

	/*------------------------------------------------------------------------------*
	 *	Arithmetic operations
	 *------------------------------------------------------------------------------*/

	
	@Override
	protected Integer visitStar(AST node) {
		return null;
	}

	@Override
	protected Integer visitDiv(AST node) {
		return null;
	}

	@Override
	protected Integer visitMod(AST node) {
		return null; 
	}

	@Override
	protected Integer visitPlus(AST node) {
		return null;
	}

	@Override
	protected Integer visitMinus(AST node) {
		return null;
	}


	/*------------------------------------------------------------------------------*
	 *	Statements
	 *------------------------------------------------------------------------------*/

	@Override
	protected Integer visitStatementSection(AST node) {
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
		return null; 
	}

	@Override
	protected Integer visitFuncList(AST node) {
		return null; 
	}

	@Override
	protected Integer visitVarUse(AST node) {
		return null; 
	}
}
