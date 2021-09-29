# Gopiler
A compiler for simple Go programs. This is a project proposed as part of the Compilers course at UFES by the Professor Eduardo Zambon.

## How to run

Open the terminal and compile the parser with
```bash
make
```

The `Main.java` file expects a flag that will determine how the AST will be used.

Flags:
  * `-c` (default)
  * `-i`

So to run the project would be
```bash
# Runs the Interpreter
make run file=<file_path> flag=-i

# Either one will run the CodeGen
make run file=<file_path>
make run file=<file_path> flag=-c
```


There is also the option to run on all files at once, but it will only run with the `-c` flag since the interpreter would cause interruptions when waiting for input
```bash
sh runall.sh
```

The NSTM has also its simulator, that will execute the given `.nstm` file
```bash
make runsim file=<file_name>.nstm
```
