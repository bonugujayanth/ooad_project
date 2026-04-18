#!/bin/bash
# ============================================================
# Healthcare System — Plain Java Build Script (no framework)
# ============================================================
set -e

PROJ_ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$PROJ_ROOT/src/main/java"
OUT="$PROJ_ROOT/out"
LIB_SQLITE="$PROJ_ROOT/lib/sqlite-jdbc.jar"
LIB_SLF4J="$PROJ_ROOT/lib/slf4j-api.jar"
LIB_SLF4J_NOP="$PROJ_ROOT/lib/slf4j-nop.jar"
CP="$LIB_SQLITE:$LIB_SLF4J:$LIB_SLF4J_NOP"
MAIN="com.healthcare.Main"

echo "[BUILD] Cleaning output..."
rm -rf "$OUT"
mkdir -p "$OUT"

echo "[BUILD] Compiling Java sources..."
find "$SRC" -name "*.java" > /tmp/sources.txt
javac --release 17 -cp "$CP" -d "$OUT" @/tmp/sources.txt

echo "[BUILD] Compilation successful."
echo ""
echo "[RUN] Starting Healthcare System..."
echo "========================================"
java -cp "$OUT:$CP" "$MAIN"
