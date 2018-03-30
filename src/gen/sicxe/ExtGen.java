package gen.sicxe;

import gen.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import ast.node.Declaration;
import ast.node.Program;
import ast.node.declare.FuncDeclaration;
import ast.node.declare.VarDeclaration;

public class ExtGen extends SicXeGen {

	public ExtGen(String name) {
		super(name);
	}
	
	@Override
	public Block gen(Program p) {
		List<String> decs = p.declarations.stream()
				.map(d -> d.id).collect(Collectors.toList());
		
		writer.start();
		writer.write(String.format("%s start 0", name), 0);
		if (!p.declarations.isEmpty()) { 
			writer.write("extdef " + list(decs), 0);
		}
		writer.write("extref _top,_fr,_ptr", 0);
		
		writer.start();
		for (Declaration d : p.declarations) {
			if (d instanceof FuncDeclaration) {
				writer.add(super.gen((FuncDeclaration)d));
			} else if (d instanceof VarDeclaration) {
				writer.add(super.gen((VarDeclaration)d));
			}
		}
		Block declarations = writer.end();
		//System.out.println(subs);
		List<String> subs = subsToList();
		if (!subs.isEmpty()) {
			writer.write("extref " + list(subs), 0);
		}
	
		writer.write("base _frame", 0);
		writer.add(declarations);
		writer.write("nobase", 0);
		writer.write("ltorg", 0);
		writer.write("_frame equ * + 2048", 0);
		writer.write("end", 0);
		return writer.end();
	}
	
	private static String list(List<String> list) {
		StringBuilder result =  new StringBuilder();
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			result.append(iter.next());
			if (iter.hasNext()) {
				result.append(",");
			}
		}
		return result.toString();
	}
	
	private List<String> subsToList() {
		List<String> result = new ArrayList<>();
		if (subs.pop)	result.add("_pop");
		if (subs.popf)	result.add("_popf");
		if (subs.push)	result.add("_push");
		if (subs.pushf) result.add("_pushf");
		if (subs.seteq) result.add("_seteq");
		if (subs.setgt) result.add("_setgt");
		if (subs.setgte)result.add("_setgte");
		if (subs.setlt) result.add("_setlt");
		if (subs.setlte)result.add("_setlte");
		return result;
	}

}
