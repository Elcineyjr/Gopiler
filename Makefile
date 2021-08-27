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

all: antlr javac
	@echo -e "\nDone."

# Documentação para a flag -Xexact-output-dir pode ser
# encontrada em https://github.com/antlr/antlr4/blob/master/doc/tool-options.md#-xexact-output-dir
antlr:
	@echo -e "Generating parser with ANTLR..."
	$(ANTLR4) -no-listener -visitor -package parser -Xexact-output-dir -o $(GEN_PATH) $(SOURCE_PATH)/GoLexer.g4 $(SOURCE_PATH)/GoParser.g4

javac:
	@echo -e "\nCompiling checker..."
	@rm -rf $(BIN_PATH)
	@mkdir $(BIN_PATH)
	$(JAVAC) $(CLASS_PATH_OPTION) -d $(BIN_PATH) $(SOURCE_PATH)/*/*.java

run:
	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) checker/Main $(FILE) 2> ast.dot
	dot -Tpdf ast.dot -o ast.pdf

# runall:
# 	@echo "Scanning $(IN) folder for files..."
# 	@-for FILE in $(IN)/*.go; do \
# 	 	echo -e "\nRunning $${FILE}" && \
# 	 	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) checker/Main $${FILE}; \
# 	done;


clean:
	@rm -rf $(GEN_PATH) $(BIN_PATH) $(SOURCE_PATH)/.antlr target/
