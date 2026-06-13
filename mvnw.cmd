@echo off
@REM Apache Maven Wrapper Script (Windows)
@REM Downloads Maven automatically on first run -- no local Maven installation required.

setlocal

@REM Find Java executable (use JAVA_HOME if set, otherwise rely on PATH)
SET JAVA_EXE=java
IF NOT [%JAVA_HOME%]==[] (
    SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
    IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
        echo ERROR: JAVA_HOME points to %JAVA_HOME% but java.exe was not found.
        exit /B 1
    )
)

@REM Resolve the project root (directory containing this script)
SET MAVEN_PROJECTBASEDIR=%~dp0
IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

SET WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

IF NOT EXIST "%WRAPPER_JAR%" (
    echo ERROR: Maven wrapper JAR not found at: %WRAPPER_JAR%
    echo Please restore the .mvn\wrapper\maven-wrapper.jar file.
    exit /B 1
)

@REM Launch the wrapper -- it downloads Maven 3.9.9 on first run and caches it in ~/.m2/wrapper/dists/
%JAVA_EXE% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*

endlocal
