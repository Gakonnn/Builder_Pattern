#!/bin/sh
set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$PROJECT_DIR"
mkdir -p build/classes
javac --release 17 -encoding UTF-8 -d build/classes src/main/java/edu/builder/travel/*.java
exec java -cp build/classes edu.builder.travel.Main "$@"
