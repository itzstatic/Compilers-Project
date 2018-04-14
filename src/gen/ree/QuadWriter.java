package gen.ree;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuadWriter {
	
	private PrintWriter writer;
	
	public QuadWriter(PrintWriter writer) {
		this.writer = writer;
	}
	
	public void write(List<Quad> quads) {
		//Max lengths
		int lenIndex = Integer.toString(quads.size() - 1).length();
		int lenMnm = quads.stream()
			.mapToInt(q -> q.mnemonic.toString().length())
			.max().getAsInt();
		int lenOp1 = quads.stream()
			.mapToInt(q -> q.op1.length())
			.max().getAsInt();
		int lenOp2 = quads.stream()
			.mapToInt(q -> q.op2.length())
			.max().getAsInt();
		int lenResult = quads.stream()
			.mapToInt(q -> q.result.length())
			.max().getAsInt();
		//wot = "%%%dd" * 5 + "\n";
		String wot = "%%%dd  " + Collections.nCopies(4, (Object) null).stream()
			.map(e -> "%%-%ds")
			.collect(Collectors.joining("  ")) + System.lineSeparator();
		String fmt = String.format(wot, lenIndex, lenMnm, lenOp1, lenOp2, lenResult);
		int index = 0;
		for (Quad quad : quads) {
			writer.printf(fmt, index, 
				quad.mnemonic.toString().toLowerCase(), 
				quad.op1.toLowerCase(), quad.op2.toLowerCase(), 
				quad.result.toLowerCase());
			index++;
		}
		
	}
}
