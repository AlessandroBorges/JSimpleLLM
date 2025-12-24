#!/bin/bash

# Reinstalando JSimpleLLM...

# Paths
JSIMPLELLM_PATH="/mnt/f/1-ProjetosIA/github/JSimpleLLM"
JAR_PATH="$JSIMPLELLM_PATH/target/JSimpleLLM-0.0.1-SNAPSHOT.jar"
POM_PATH="$JSIMPLELLM_PATH/pom.xml"

# Verificar se JAR existe
if [ ! -f "$JAR_PATH" ]; then
    echo "JAR nao encontrado. Compilando..."
    cd "$JSIMPLELLM_PATH"
    mvn clean package -DskipTests
    cd -
fi

# Instalar
echo "Instalando no repositorio local..."
echo "$JAR_PATH"
mvn install:install-file -Dfile="$JAR_PATH" -DpomFile="$POM_PATH"

echo "JSimpleLLM instalado com sucesso!"
echo
echo "Proximos passos:"
echo "  cd /mnt/f"
echo "  cd /mnt/f/1-ProjetosIA/github/KSimpleRag"
echo "  mvn clean compile"