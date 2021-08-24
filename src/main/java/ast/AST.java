package ast;

import static typing.Type.NO_TYPE;

import java.util.ArrayList;
import java.util.List;

import tables.VarTable;
import typing.Type;

public class AST {
	public  final NodeKind kind;
	public  final int intData;
	public  final float floatData;
	public  final Type type;
	private final List<AST> children;

	private AST(NodeKind kind, int intData, float floatData, Type type) {
		this.kind = kind;
		this.intData = intData;
		this.floatData = floatData;
		this.type = type;
		this.children = new ArrayList<AST>();
	}

	public AST(NodeKind kind, int intData, Type type) {
		this(kind, intData, 0.0f, type);
	}

	public AST(NodeKind kind, float floatData, Type type) {
		this(kind, 0, floatData, type);
	}

	// Add child to node
	public void addChild(AST child) {
		this.children.add(child);
	}

	// Get child at given index
	public AST getChild(int idx) {
		if(idx >= 0 && idx <= this.children.size()) {
			return this.children.get(idx);
		}
		return null;
	}

	// Add all children to node
	public static AST newSubtree(NodeKind kind, Type type, AST... children) {
		AST node = new AST(kind, 0, type);
	    for (AST child: children) {
	    	node.addChild(child);
	    }
	    return node;
	}

	public void print() {
		System.out.println(this.kind);
		System.out.println(this.type);
		System.out.println(this.children);
		System.out.println(this.children.size());
	}

	// --------------------------------- Testing -------------------------------
	
	// Variáveis internas usadas para geração da saída em DOT.
	// Estáticas porque só precisamos de uma instância.
	private static int nr;
	private static VarTable vt;

	// Imprime recursivamente a codificação em DOT da subárvore começando no nó atual.
	// Usa stderr como saída para facilitar o redirecionamento, mas isso é só um hack.
	private int printNodeDot() {
		int myNr = nr++;

	    System.err.printf("node%d[label=\"", myNr);
	    if (this.type != NO_TYPE) {
	    	System.err.printf("(%s) ", this.type.toString());
	    }
	    if (this.kind == NodeKind.VAR_DECL_NODE || this.kind == NodeKind.VAR_USE_NODE) {
	    	System.err.printf("%s@", vt.getName(this.intData));
	    } else {
	    	System.err.printf("%s", this.kind.toString());
	    }
	    if (NodeKind.hasData(this.kind)) {
	        if (this.kind == NodeKind.BOOL_VAL_NODE) {
	        	System.err.printf("%.2f", this.floatData);
	        } else if (this.kind == NodeKind.STRING_VAL_NODE) {
	        	System.err.printf("@%d", this.intData);
	        } else {
	        	System.err.printf("%d", this.intData);
	        }
	    }
	    System.err.printf("\"];\n");

	    for (int i = 0; i < this.children.size(); i++) {
			// System.out.println("size " + this.children.size());
			// System.out.println("i "+ i);
			// System.out.println(this.children.get(i));
			// System.out.println(this.children.get(i) == null);
			// System.out.println();
			// if(this.children.get(i) == null) {
			// 	continue;
			// } 

			int childNr = this.children.get(i).printNodeDot();
			System.err.printf("node%d -> node%d;\n", myNr, childNr);
			
	    }
	    return myNr;
	}

	// Imprime a árvore toda em stderr.
	public static void printDot(AST tree, VarTable table) {
	    nr = 0;
	    vt = table;
	    System.err.printf("digraph {\ngraph [ordering=\"out\"];\n");
	    tree.printNodeDot();
	    System.err.printf("}\n");
	}
}