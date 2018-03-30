

import gen.Block;
import gen.sicxe.SicXeGen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import parse.Input;
import parse.Lexer;
import parse.Parser;
import semantic.Checker;
import semantic.Symtab;
import ast.CompileError;
import ast.node.Program;


public class Start {
	
	public static void main(String[] args) throws IOException {
//		test();
//		System.exit(0);
		
		if (args.length < 1) {
			System.err.println("Requires file name!");
			System.exit(2);
		}
		
		String fileName = args[0];
		Reader reader = new FileReader(fileName);
		
		//Parsing with symtab
		Parser parser = new Parser(new Lexer(new Input(reader, 2, 4)));
		Program p = parser.program();

		//Semantic check
		p.accept(new Checker(new Symtab()));
	

		String mainName = fileName.split("\\.")[0];
		//Codegen
		SicXeGen gen;
//		if (p.runnable) {
//			gen = new SicXeGen(mainName);
//		} else {
//			gen = new ExtGen(mainName);
//		}
		gen = new SicXeGen(mainName);
		Block b = gen.gen(p);
		
		PrintWriter w = new PrintWriter(new File(mainName + ".asm"));
		
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
		for (int i = 1; i <= 54; i++) {
			Reader reader = new FileReader(new File("files/test" + i + ".txt"));
			Parser parser = new Parser(new Lexer(new Input(reader, 3, 4)));
			try {
				Symtab s = new Symtab();
				//new SymtabInit().init(s);
				Checker c = new Checker(s);
				parser.program().accept(c);
				System.out.println("Accepted " + i);
			} catch (CompileError e) {
				
//				System.err.println("ON FILE " + i);
//				e.printStackTrace();
			}
		}
	}
}
