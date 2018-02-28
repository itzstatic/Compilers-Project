package gen.block;

import gen.Block;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ParentBlock extends Block {
	public List<Block> children = new ArrayList<>();
	
	public void add(Block b) {
		children.add(b);
		size += b.size;
	}

	@Override
	public void write(PrintWriter w) {
		for (Block child : children) {
			child.write(w);
		}
	}
}
