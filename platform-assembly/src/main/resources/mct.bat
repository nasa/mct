@echo off

set _MCT_BASE=%~dp0
set _MCT_LIB=lib
set _MCT_LAUNCHER_JAR=%_MCT_LIB%\startup-${project.version}.jar
set _JAVA_EXE=java

cd %_MCT_BASE%

%_JAVA_EXE% -jar "%_MCT_LAUNCHER_JAR%"