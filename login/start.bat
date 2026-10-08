@echo off
cd /d "%~dp0"

if not exist build\classes (
  echo ERROR: build\classes vide. Lance d'abord build.bat
  exit /b 1
)

set CP=build\classes;lib\*;libs\*
java -Xmx512m -cp "%CP%" org.starloco.locos.kernel.Main %*
