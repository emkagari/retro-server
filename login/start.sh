#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -d build/classes ] || [ -z "$(ls -A build/classes 2>/dev/null)" ]; then
  echo "ERROR: build/classes vide. Lance d'abord ./build.sh"
  exit 1
fi

CP="build/classes:lib/*:libs/*"
exec java -Xmx512m -cp "$CP" org.starloco.locos.kernel.Main "$@"
