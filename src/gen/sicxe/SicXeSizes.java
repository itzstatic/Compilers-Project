package gen.sicxe;

import ast.Sizes;
import ast.TypeId;

public class SicXeSizes implements Sizes {

	@Override
	public int sizeOf(TypeId typeId) {
		switch(typeId) {
		case FLOAT: return 6;
		case INT: return 3;
		case VOID: return 0;
		}
		throw new IllegalStateException();
	}

	@Override
	public int sizeOfPtr() {
		return 3; //1 word
	}

}
