@echo off
@REM Wrapper do Maven para SubTrack
@REM Usa o Maven instalado em %USERPROFILE%\.maven\apache-maven-3.9.14

setlocal

set "MAVEN_HOME=%USERPROFILE%\.maven\apache-maven-3.9.14"
set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"

if not exist "%MVN_CMD%" (
    echo ERRO: Maven não encontrado em %MAVEN_HOME%
    echo Por favor, faça o download do Maven 3.9.14 e extraia-o para %USERPROFILE%\.maven\
    exit /B 1
)

call "%MVN_CMD%" %*

endlocal
