REM @echo off
REM If necessary adjust the search PATH to include Java8 JAVAC and JAR.
PATH=%PATH%;c:\Program Files\Java\jdk1.8.0_161\bin
set CURRENT_DIRECTORY=%cd%
set WORKING_DIRECTORY=%~dp0

cd /d "%WORKING_DIRECTORY%"
javac -d bin src/*.java
jar cfm html/_search/Indexer.jar META-INF/MANIFEST.MF -C bin .

cd /d "%CURRENT_DIRECTORY%"