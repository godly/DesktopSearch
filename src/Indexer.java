import java.util.regex.Pattern;

public class Indexer {
	public final static String constOutputFile = "filelist.js";
	public final static boolean constUseRelativePath = true;
	public final static long constResultLimit = 10000;
	
	public final static String constOptSearch = "search";
	public final static String constOptOutputFile = "out";
	public final static String constOptExclude = "exclude";
	public final static String constOptInclude = "include";
	public final static String constOptMetaDataSeperator = "mds";
	public final static String constOptAbsolutePath = "abs";
	public final static String constOptIcuFile = "icu";
	public final static String constOptResultLimit = "limit";
	public final static String constOptDebug = "debug";
	
	public static boolean debugMode = false;
	
	public final static void main(String[] args) throws Exception {
		new Indexer();
	}
	public final Object clone() throws CloneNotSupportedException {
	    throw new CloneNotSupportedException();
	}
	
	public final void printUsage() {
		System.out.println("Usage: java [options] -jar Indexer.jar");
		System.out.println("Options:"); 
		System.out.println("-D" + constOptSearch + "=C:/temp/ for the search path where the recursive search starts. Required!");
		System.out.println("-D" + constOptOutputFile + "=" + constOutputFile + " for the output file name and location. Default: " + constOutputFile);
		System.out.println("-D" + constOptExclude + "=\"^./_search/.*$\" to exclude the search folder. Default: none");
		System.out.println("-D" + constOptInclude + "=\"^.*\\.(doc|pdf|txt)$\" to include only some document types. Default: none");
		System.out.println("-D" + constOptResultLimit + "=" + constResultLimit + " Limit the index size to n elements. Set to 0 for no limit. Default: " + constResultLimit);
		System.out.println("-D" + constOptDebug + " Be a little bit more verbose, write the used parameters to the output file.");
		System.out.println("-D" + constOptIcuFile + " For transliteration of Cyrillic characters to ASCII configure the  ICU4J JAR file. (Eg /tmp/icu4j-60_2.jar)");
		System.out.println("-D" + constOptMetaDataSeperator + "=\"[&!/,\\\\._;:\\\\-\\\"\\'()\\\\[\\\\]]\" to specify additional characters to seperate the words. ' ' will always be used to seperate words when gernerating meta data.");
		System.out.println("-D" + constOptAbsolutePath + " Output and process (exclude and include filters) absolut pathes.");
	}
	
	public Indexer() {
		String searchPath = System.getProperty(constOptSearch, null);
		String outputFile = System.getProperty(constOptOutputFile, constOutputFile);
		String excludeFilter = System.getProperty(constOptExclude, null);
		String includeFilter = System.getProperty(constOptInclude, null);
		String metaDataSeperator = System.getProperty(constOptMetaDataSeperator, null);
		String icuFile = System.getProperty(constOptIcuFile, null);
		long resultLimit = constResultLimit;
		try {
			resultLimit = Integer.parseInt(System.getProperty(constOptResultLimit, null));
		} catch (Exception e) {
		}
		if (resultLimit < 1) {
			resultLimit = Long.MAX_VALUE;
		}
		
		boolean useRelativePath = constUseRelativePath;
		if (System.getProperty(constOptAbsolutePath, null) != null ) {
			useRelativePath = false;
		}
		
		if (System.getProperty(constOptDebug, null) != null ) {
			debugMode = true;
		}
		if (searchPath == null) {
			printUsage();
			System.exit(1);
		}
		Pattern excludePattern = null;
		if (excludeFilter != null) {
			excludePattern = Pattern.compile(excludeFilter);
		}
		Pattern includePattern = null;
		if (includeFilter != null) {
			includePattern = Pattern.compile(includeFilter);
		}
		
		SearchFiles sf = SearchFiles.getInstance();
		if (false == sf.initialize(searchPath, outputFile, excludePattern, includePattern, resultLimit, metaDataSeperator, useRelativePath, icuFile) ) {
			System.err.println("FATAL Initialization failed.");
			System.exit(1);
		}
		String debugData = null;
		if (debugMode) {
			debugData = "Parameters: -Ddebug";
			debugData = debugData + " -D" + constOptSearch + "=\"" + searchPath + "\"";
			if (!constOutputFile.equalsIgnoreCase(outputFile)) {
				debugData = debugData + " -D" + constOptOutputFile + "=\"" + outputFile + "\"";
			}
			if (excludeFilter != null) {
				debugData = debugData + " -D" + constOptExclude + "=\"" + excludeFilter + "\"";
			}
			if (includeFilter != null) {
				debugData = debugData + " -D" + constOptInclude + "=\"" + includeFilter + "\"";
			}
			if (metaDataSeperator != null) {
				debugData = debugData + " -D" + constOptMetaDataSeperator + "=\"" + metaDataSeperator + "\"";
			}
			if (resultLimit != constResultLimit) {
				debugData = debugData + " -D" + constResultLimit + "=" + resultLimit;
			}
	
			if (constUseRelativePath != useRelativePath) {
				debugData = debugData + " -D" + constOptAbsolutePath;	
			}
			if (icuFile != null) {
				debugData = debugData + " -D" + constOptIcuFile + "=\"" + icuFile + "\"";
			}
			// replace * with ×. '×/' will not terminate the comment
			debugData = debugData.replaceAll("\\*", "×");
			debugData = "/* " + debugData + " */\n";
			sf.setDebugData(debugData);
		}
		sf.scanDirectory();
	}
}
