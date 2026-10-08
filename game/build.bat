@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

where java >nul 2>&1    || (echo ERROR: 'java' introuvable dans le PATH ^(JDK 11+ requis^) & exit /b 1)
where javac >nul 2>&1   || (echo ERROR: 'javac' introuvable dans le PATH ^(JDK 11+ requis^) & exit /b 1)

echo === Compilation aegnor_gameV2 ^(full Java^) ===
if not exist build\classes mkdir build\classes

set CP=
for %%f in (librerias\*.jar) do set CP=!CP!%%f;

rem Liste des .java avec des slash avant (/) : un argfile javac traite l'antislash
rem comme un echappement, ce qui casse les chemins absolus Windows.
if exist "%TEMP%\game_java.txt" del "%TEMP%\game_java.txt"
for /f "delims=" %%f in ('dir /s /b src\*.java') do (
  set "p=%%f"
  set "p=!p:\=/!"
  echo !p!>> "%TEMP%\game_java.txt"
)

javac -encoding UTF-8 -cp "!CP!" -d build\classes "@%TEMP%\game_java.txt"
if errorlevel 1 exit /b 1

echo === Build OK ===
echo Lance ensuite start.bat
exit /b 0
