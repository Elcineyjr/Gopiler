package typing;

public enum Type {
	INT_TYPE {
		public String toString() {
			return "int";
		}
	},
	FLOAT32_TYPE {
		public String toString() {
			return "float32";
		}
	},
	BOOL_TYPE {
		public String toString() {
			return "bool";
		}
	},
	STRING_TYPE {
		public String toString() {
			return "string";
		}
	}
}
