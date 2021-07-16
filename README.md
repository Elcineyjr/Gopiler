# Gopiler
A compiler implementation for simple programs in Go. This is a project proposed as part of the Compilers course at UFES by the Professor Eduardo Zambon.

## How to run

Create a file called `.env` and add a variable with the folder's path (the one wich the repository is). Should be something like
```env
root=/home/user/Desktop
```

Now open the terminal and compile the parser with
```
make
```

And run it by either providing a file or running on all files from the tests folder
```
make run FILE=<file_path>
make runall
```
