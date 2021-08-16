package typing;

public enum Type {
	INT_TYPE, 
	FLOAT32_TYPE, 
	BOOL_TYPE, 
	STRING_TYPE,
	NO_TYPE; 

	@Override
  	public String toString() {
		switch(this) {
			case INT_TYPE: return "int";
			case FLOAT32_TYPE: return "float32";
			case BOOL_TYPE: return "bool";
			case STRING_TYPE: return "string";
			case NO_TYPE: return "no_type";
			default: throw new IllegalArgumentException();
    	}
	}
}
