

import gen.Block;
import gen.SicXeGen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import semantic.Checker;
import semantic.Symtab;
import ast.CompileError;
import ast.node.Program;


public class Start {
	
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Requires file name!");
			System.exit(2);
		}
		
		String fileName = args[0];
		
		Reader reader = null;
		try {
			reader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			System.err.println("Cannot open file \"" + fileName + "\"!");
			System.exit(1);
		}
		
		//Project 1: Lexer
//		Lexer lexer = new Lexer(new Input(reader, 2, 4));
//		Token token;
//		while ((token = lexer.next()) != null) {
//			if (token.type == TokenType.SPECIAL) {
//				System.out.println(token.payload);
//			} else {
//				System.out.println(token.type + ": " + token.payload);
//			}
//		}
		
		//Parsing with symtab
		Parser parser = new Parser(new Lexer(new Input(reader, 2, 4)));
		Program p = parser.program();
		PrintWriter w = new PrintWriter(new File("output.txt"));
		
		//Semantic check
		p.accept(new Checker(new Symtab()));
		
		//Codegen
		SicXeGen gen =  new SicXeGen();
		Block b = gen.gen(p);
		b.write(w);
		w.flush();
		System.out.println("Compilation complete.");
		w.close();
		//GUI IDE
//		Ide ide = new Ide();
//		ide.addButtonListener(e -> {
//			StringReader r = new StringReader(ide.getSourceCode());
//			Parser p = new Parser(new Lexer(new Input(r, 2, 4)));
//			try {
//				ide.showFeedback("Compiling...", true);
//				Program pr = p.program();
//				pr.accept(new Checker(new Symtab()));
//				ide.showFeedback("Accept", true);
//			} catch (CompileError ce) {
//				ide.showFeedback(ce.getMessage(), false);
//				ide.highlight(ce.start.pos, ce.end.pos);
//			}
//		});
	}
	
	public static void test() throws FileNotFoundException {
		//30 test files
		for (int i = 1; i <= 59; i++) {
			Reader reader = new FileReader(new File("input" + i + ".txt"));
			Parser parser = new Parser(new Lexer(new Input(reader, 3, 4)));
			try {
				parser.program();
				//Checker c = new Checker(new Symtab());
				//p.accept(c);
			} catch (CompileError e) {
				if (i <= 29) {
					continue;
				}
				System.err.println("ON FILE " + i);
				e.printStackTrace();
			}
		}
	}
}
