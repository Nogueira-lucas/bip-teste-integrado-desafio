#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "========================================"
echo "  bip-teste-integrado — Build"
echo "========================================"
echo ""

# Roda da raiz: Maven instala o POM pai em ~/.m2 primeiro,
# depois builda ejb-module e backend-module na ordem declarada em <modules>.
cd "$ROOT_DIR"
mvn clean install

echo ""
echo "========================================"
echo "  Build concluído com sucesso!"
echo "========================================"
echo ""
echo "Para executar o backend:"
echo "  java -jar backend-module/target/backend-module-0.0.1-SNAPSHOT.jar"
echo ""
echo "Lembre-se de subir o banco antes:"
echo "  docker compose up -d"
