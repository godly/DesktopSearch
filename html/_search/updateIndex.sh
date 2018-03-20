#!/bin/bash
# Adjust search PATH to include Java8 JAVAC and JAR if needed
# PATH=$PATH:/usr/...
currentDir=$(pwd)
workingDir=$(dirname $(readlink -f "$0"))

# Change into the working directory (DesktopSearch/html/)
cd "${workingDir}"

# TODO Define the search directory here TODO
cd ..
searchDir=$(pwd)
cd "${workingDir}"

# Create forward slashes '/' (or '\\') for Windows
# NOP

# Index the current directory
# -Ddebug - add more info to filelist.js
# -Dicu=icu4j-60_2.jar - enable transliteration
# -Dsearch=... - specify the search directory (required)
# -Dout=... - specify the file list
# -Dexlude=... - specify an exclude filter (eg: -Dexclude="^./_search$|^.*.thumb$")
# -Dinclude=... - specify an include filter (eg: -Dinclude="^.*\.(mp4|mkv)")
# -Dlimit=... - limit the number of results (eg: -Dlimit=10000)
# -Dmds=... - specify a meta data seperator to split the file names (default: "[&!/,_;:\\-\"\'()\\[\\]]")
# -Dabs - use absolte paths, also for the filters
java -Ddebug -Dicu=icu4j-60_2.jar -Dsearch="${searchDir}" -Dout=filelist.js -jar Indexer.jar
