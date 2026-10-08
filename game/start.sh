#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -d build/classes ] || [ -z "$(ls -A build/classes 2>/dev/null)" ]; then
  echo "ERROR: build/classes vide. Lance d'abord ./build.sh"
  exit 1
fi

if [ ! -f config.properties ]; then
  echo "WARN: config.properties manquant — il sera généré au premier démarrage. Édite-le et relance."
fi

CP="build/classes:librerias/*"
exec java -Xmx2g -cp "$CP" kernel.Main "$@"
