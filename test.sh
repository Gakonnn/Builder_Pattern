#!/bin/sh
set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$PROJECT_DIR"
mkdir -p build/test-classes
javac --release 17 -encoding UTF-8 -d build/test-classes \
    src/main/java/edu/builder/travel/*.java \
    src/test/java/edu/builder/travel/*.java
exec java -cp build/test-classes edu.builder.travel.TravelBuilderTest
