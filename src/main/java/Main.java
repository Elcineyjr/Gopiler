

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import checker.SemanticChecker;
import code.CodeGen;
import code.Interpreter;
import parser.GoLexer;
import parser.GoParser;

public class Main {
	public static void main(String[] args) throws IOException {
		CharStream input = CharStreams.fromFileName(args[0]);
		String flag = args[1];

		GoLexer lexer = new GoLexer(input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		GoParser parser = new GoParser(tokens);

		ParseTree tree = parser.program();

		if (parser.getNumberOfSyntaxErrors() != 0) {
			return;
		}

		SemanticChecker checker = new SemanticChecker();
		checker.visit(tree);

		if(flag.equals("-i")){
			Interpreter interpreter = new Interpreter(checker.st, checker.vt, checker.ft);
			interpreter.execute(checker.root);
		} else {
			CodeGen codeGen = new CodeGen(checker.st, checker.vt);
			codeGen.execute(checker.root);
		}
	}

}
