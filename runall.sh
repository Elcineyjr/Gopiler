#!/bin/bash

ROOT=$(pwd)
IN=$ROOT/tests
OUT=./out

SOURCE_PATH=src/main/java
BIN_PATH=$SOURCE_PATH/bin

ANTLR_PATH=$ROOT/tools/antlr-4.9.2-complete.jar
CLASS_PATH_OPTION="-cp .:$ANTLR_PATH"

# code generation flag because the interpreter flag will stop execution in some files for input
flag=-c

rm -rf $OUT
mkdir -p $OUT
for infile in `ls $IN/*.go`; do
    base=$(basename $infile)
    outfile=$OUT/${base/.go/.nstm}
    echo
    echo Compiling $base
    java $CLASS_PATH_OPTION:$BIN_PATH Main $infile $flag > $outfile 
done