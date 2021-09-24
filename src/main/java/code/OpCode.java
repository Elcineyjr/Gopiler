package code;

/*
 * Instructions based on MIPS.
 * https://opencores.org/projects/plasma/opcodes 
 */
public enum OpCode {
    HALT("HALT", 0),
    NOOP("NOOP", 0);
	
	public final String name;
	public final int opCount;
	
	private OpCode(String name, int opCount) {
		this.name = name;
		this.opCount = opCount;
	}
	
	public String toString() {
		return this.name;
	}
	
}


