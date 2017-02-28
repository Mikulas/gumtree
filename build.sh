#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd "$DIR"

set -x
./gradlew build -x test

cd "$DIR/dist/build/distributions"

rm -rf gumtree-*-2.1.0-SNAPSHOT/
unzip *.zip
