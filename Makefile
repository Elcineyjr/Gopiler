JAVA=java
JAVAC=javac

# O ANTLR deve estar instalado numa pasta chamada tools na raiz do projeto
ANTLR_PATH=tools/antlr-4.9.2-complete.jar
CLASS_PATH_OPTION=-cp .:$(ANTLR_PATH)

# Comandos como descritos na página do ANTLR.
ANTLR4=$(JAVA) -jar $(ANTLR_PATH)

# Diretório com o código fonte.
SOURCE_PATH=src/main/java

# Diretório para aonde vão os arquivos gerados pelo ANTLR.
GEN_PATH=$(SOURCE_PATH)/parser

# Diretório aonde está a classe com a função main.
MAIN_PATH=$(SOURCE_PATH)/checker

# Diretório para os arquivos .class
BIN_PATH=$(SOURCE_PATH)/bin

# Diretório para os casos de teste
IN=tests

# Flag para executar a geração de código
# caso queira se executar o interpretador basta alterar a flag para -i
flag=-c

all: antlr javac simulator
	@echo -e "\nDone."

# Documentação para a flag -Xexact-output-dir pode ser
# encontrada em https://github.com/antlr/antlr4/blob/master/doc/tool-options.md#-xexact-output-dir
antlr:
	@echo -e "Generating parser with ANTLR..."
	$(ANTLR4) -no-listener -visitor -package parser -Xexact-output-dir -o $(GEN_PATH) $(SOURCE_PATH)/GoLexer.g4 $(SOURCE_PATH)/GoParser.g4

javac:
	@echo -e "\nCompiling Interpreter and CodeGen..."
	@rm -rf $(BIN_PATH)
	@mkdir $(BIN_PATH)
	$(JAVAC) $(CLASS_PATH_OPTION) -d $(BIN_PATH) $(SOURCE_PATH)/Main.java $(SOURCE_PATH)/*/*.java

run:
	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) Main $(file) $(flag)

simulator:
	@echo -e "\nCompiling the NSTM Simulator..."
	gcc -Wall -Wconversion -o NSTMsimulator nstm/*.c 

runsim:
	./NSTMsimulator < $(file)

clean:
	@rm -rf $(GEN_PATH) $(BIN_PATH) $(SOURCE_PATH)/.antlr target/ out/ *.dot *.pdf
