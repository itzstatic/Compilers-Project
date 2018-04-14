package gen.ree;

import ast.Sizes;
import ast.TypeId;

public class ReeSizes implements Sizes {

	@Override
	public int sizeOf(TypeId typeId) {
		switch(typeId) {
		case FLOAT: return 4;
		case INT: return 4;
		case VOID: return 0;
		}
		throw new IllegalStateException();
	}

	@Override
	public int sizeOfPtr() {
		return 4;
	}

}
