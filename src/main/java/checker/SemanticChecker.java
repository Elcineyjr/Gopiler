package checker;

import org.antlr.v4.runtime.Token;

import parser.GoParser;
import parser.GoParserBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import typing.Type;


public class SemanticChecker extends GoParserBaseVisitor<Void> {

	private StrTable st = new StrTable(); // Tabela de strings.
	private VarTable vt = new VarTable(); // Tabela de variáveis.

	Type lastDeclType; // Variável "global" com o último tipo declarado.

	private boolean passed = true;

	// Testa se o dado token foi declarado antes.
	void checkVar(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = vt.lookupVar(text);
		if (idx == -1) {
			System.err.printf("SEMANTIC ERROR (%d): variable '%s' was not declared.\n", line, text);
			passed = false;
			return;
		}
	}

	// Cria uma nova variável a partir do dado token.
	void newVar(Token token) {
		String text = token.getText();
		int line = token.getLine();
		int idx = vt.lookupVar(text);
		if (idx != -1) {
			System.err.printf("SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n", line, text,
					vt.getLine(idx));
			passed = false;
			return;
		}
		vt.addVar(text, line, lastDeclType);
	}

	// Retorna true se os testes passaram.
	boolean hasPassed() {
		return passed;
	}

	// Exibe o conteúdo das tabelas em stdout.
	void printTables() {
		System.out.print("\n\n");
		System.out.print(st);
		System.out.print("\n\n");
		System.out.print(vt);
		System.out.print("\n\n");
	}

}
