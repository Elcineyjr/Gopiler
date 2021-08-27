#!/bin/bash

ROOT=$(pwd)
IN=$ROOT/tests
OUT=./out

SOURCE_PATH=src/main/java
BIN_PATH=$SOURCE_PATH/bin

ANTLR_PATH=$ROOT/tools/antlr-4.9.2-complete.jar
CLASS_PATH_OPTION="-cp .:$ANTLR_PATH"

rm -rf $OUT
mkdir $OUT
for infile in `ls $IN/*.go`; do
    base=$(basename $infile)
    outfile=$OUT/${base/.go/.out}
    dotfile=$OUT/${base/.go/.dot}
    pdffile=$OUT/${base/.go/.pdf}
    echo Running $base
    java $CLASS_PATH_OPTION:$BIN_PATH checker/Main $infile 1> $outfile 2> $dotfile
    dot -Tpdf $dotfile -o $pdffile
done