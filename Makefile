include .env

JAVA=java
JAVAC=javac

# Diretório raiz do projeto (variável root vinda do arquivo .env)
# Altere o seu arquivo .env e coloque o caminho que você estiver usando
REPO=${root}/Gopiler

# O ANTLR deve estar instalado numa pasta chamada tools na raiz do projeto
ANTLR_PATH=$(REPO)/tools/antlr-4.9.2-complete.jar
CLASS_PATH_OPTION=-cp .:$(ANTLR_PATH)

# Comandos como descritos na página do ANTLR.
ANTLR4=$(JAVA) -jar $(ANTLR_PATH)
GRUN=$(JAVA) $(CLASS_PATH_OPTION) org.antlr.v4.gui.TestRig

# Diretório para aonde vão os arquivos gerados.
GEN_PATH=parser

# Diretório para os casos de teste
IN=$(REPO)/tests

all: antlr javac
	@echo "Done."

antlr: GoLexer.g4 GoParser.g4
	$(ANTLR4) -no-listener -o $(GEN_PATH) GoLexer.g4 GoParser.g4

javac:
	$(JAVAC) $(CLASS_PATH_OPTION) $(GEN_PATH)/*.java

run:
	cd $(GEN_PATH) && $(GRUN) Go program $(REPO)/$(FILE)

runall:
	-for FILE in $(IN)/*.go; do \
	 	cd $(GEN_PATH) && \
	 	echo -e "\nRunning $${FILE}" && \
	 	$(GRUN) GoLexer tokens -tokens $${FILE} && \
	 	cd .. ; \
	done;

clean:
	@rm -rf $(GEN_PATH)
