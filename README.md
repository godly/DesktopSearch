# DesktopSearch
Javascript / HTML5 based search engine designed to search directories and network shares. The indexer to build the index requires Java 8.

### Getting started
To get started clone or download the zip archive and extract it.
For the local search engine [Elasticlunr.js](https://github.com/weixsong/elasticlunr.js/releases/tag/v0.9.6) is used (MIT license).
For transliteration support during index creation [ICU](http://download.icu-project.org/files/icu4j/60.2/) is used (Unicode license).
The 10 MB icu4j-60_2.jar file can be deleted if transliteration of cyrillic characters is not needed.

### Build
You may skip this step.
Run the build.cmd (or .sh) script to compile the classes and to build the executable jar file.
For easy redistribution of the whole package the JAR file in placed in the html/_search directory.

### Stopwords
Common words like "a", "and", etc. should not be used for search.
Sample stopwords for various languages are in the stopWords directory.

### Limitations
As the client is Javascript based the index should not contain more than 10,000 documents. Otherwise loading the html/search.html page takes very long of may crash the browser.

### Initial Usage
To start the local search engine open html/search.html. The documents in html are already indexed, search for "sea*" or "search".

### Installation and Setup
Copy search.html and the _search folder to a random directory to index.
Delete the icu4j-60_2.jar file if it is not needed.
Adjust the search path in the _search/updateIndex.cmd (or .sh) script if needed.
Copy a stopwords list to _search/stopwords.js.
Run the _search/updateIndex.cmd (or .sh) script to build the index (_search/filelist.js) and adjust the exclude and include filter as needed.
Launch search.html and start searching.

To keep the index small scanning a whole drive must be avoided. Scanning relevant folders and a limitation to specific file types is recommended.
The "Installation and Setup" process can be repeated as often as desired. Without the transliteration JAR the the installation size is 50kB plus 1-2 MB for the index. 
### Indexer
The java indexer has the following command line options:
```-Dsearch=C:/temp/ for the search path where the recursive search starts. Required!
-Dout=filelist.js for the output file name and location. Default: filelist.js
-Dexclude="^./_search/.*$" to exclude the search folder. Default: none
-Dinclude="^.*\.(doc|pdf|txt)$" to include only some document types. Default: none
-Dlimit=10000 Limit the index size to n elements. Set to 0 for no limit. Default: 10000
-Dicu=icu4j-60_2.jar For transliteration of Cyrillic characters to ASCII specify the ICU4J JAR file including path.
-Ddebug Be a little bit more verbose, write the used parameters to the output file.
-Dmds="[&!/,\\._;:\\-\"\'()\\[\\]]" to specify additional characters to seperate the words. ' ' will always be used to seperate words when gernerating the meta data.
-Dabs Output and process (exclude and include filters) absolut pathes.
```
The `exclude` and `include` filters should be used, use regular expressions.
The `debug`, `limit` and `icu` parameters may be set.
The `mds` and `abs` options are usually not needed.