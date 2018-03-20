REM @echo off
REM Adjust search PATH to include Java8 JAVAC and JAR if needed
PATH=%PATH%;c:\Program Files\Java\jdk1.8.0_161\bin
set CURRENT_DIRECTORY=%cd%
set WORKING_DIRECTORY=%~dp0

REM Change into the target directory (DesktopSearch/html/)
cd /d "%WORKING_DIRECTORY%"

REM TODO Define the search directory here TODO
cd ..
set SEARCH_DIRECTORY=%CD%
cd /d "%WORKING_DIRECTORY%"

REM Create forward slashes '/' (or '\\') for Windows
set SEARCH_DIRECTORY_ESC=%SEARCH_DIRECTORY:\=/%

REM Index the current directory
REM -Ddebug - add more info to filelist.js
REM -Dicu=icu4j-60_2.jar - enable transliteration
REM -Dsearch=... - specify the search directory (required)
REM -Dout=... - specify the file list
REM -Dexlude=... - specify an exclude filter (eg: -Dexclude="^./_search$|^.*.thumb$")
REM -Dinclude=... - specify an include filter (eg: -Dinclude="^.*\.(mp4|mkv)")
REM -Dlimit=... - limit the number of results (eg: -Dlimit=10000)
REM -Dmds=... - specify a meta data seperator to split the file names (default: "[&!/,_;:\\-\"\'()\\[\\]]")
REM -Dabs - use absolte paths, also for the filters
java -Ddebug -Dicu=icu4j-60_2.jar -Dsearch=%SEARCH_DIRECTORY_ESC% -Dout=filelist.js -jar Indexer.jar

cd /d "%CURRENT_DIRECTORY%"