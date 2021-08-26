package tables;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import typing.Type;

// TODO: implement hash table
public final class VarTable {

	private List<Entry> table = new ArrayList<Entry>();

	public int lookupVar(String s) {
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).name.equals(s)) {
				return i;
			}
		}
		return -1;
	}

	public int addVar(String s, int line, Type type, int argSize) {
		Entry entry = new Entry(s, line, type, argSize);
		int idxAdded = table.size();
		table.add(entry);
		return idxAdded;
	}

	public String getName(int i) {
		return table.get(i).name;
	}

	public int getLine(int i) {
		return table.get(i).line;
	}

	public Type getType(int i) {
		return table.get(i).type;
	}

	public int getArgSize(int i) {
		return table.get(i).argSize;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Variables table:\n");
		for (int i = 0; i < table.size(); i++) {
			f.format("Entry %d -- name: %s, line: %d, type: %s\n", i, getName(i), getLine(i), getType(i).toString());
		}
		f.close();
		return sb.toString();
	}

	private final class Entry {
		String name;
		int line;
		Type type;
		int argSize;


		Entry(String name, int line, Type type, int argSize) {
			this.name = name;
			this.line = line;
			this.type = type;
			this.argSize = argSize;
		}
	}
}
