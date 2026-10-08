@echo off
cd /d "%~dp0"

if not exist build\classes (
  echo ERROR: build\classes vide. Lance d'abord build.bat
  exit /b 1
)

if not exist config.properties (
  echo WARN: config.properties manquant — il sera genere au premier demarrage. Edite-le et relance.
)

set CP=build\classes;librerias\*
java -Xmx2g -cp "%CP%" kernel.Main %*
