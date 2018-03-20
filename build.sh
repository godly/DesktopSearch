# If necessary adjust the search PATH to include Java8 JAVAC and JAR.
# PATH=$PATH:/usr/...
currentDir=`pwd`
workingDir=$(dirname $(readlink -f "$0"))

cd "${workingDir}"
javac -d bin src/*.java
jar cfm html/_search/Indexer.jar META-INF/MANIFEST.MF -C bin .

cd "${currentDir}"
