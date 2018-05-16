import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

//import com.ibm.icu.text.Transliterator;

public class SearchFiles {
	public final static Charset constCharsetUTF8 = StandardCharsets.UTF_8;
	public final static String constMetaDataSeperator = "[&!/,_;:\\-\"\'()\\[\\]]";

	private static SearchFiles instance = null;
	private boolean isInitialized = false;
	private TransliterationHelper transliterationInstance = null;

	private String searchPath = null;
	private String outputFile = null;
	private Pattern excludePattern = null;
	private Pattern includePattern = null;
	private boolean useRelativePath = true;
	private String metaDataSeperator = null;
	private String debugData = null;
	private long resultLimit = 0;

	private SearchFiles() {
	}

	public final static synchronized SearchFiles getInstance() {
		if (instance == null) {
			instance = new SearchFiles();
		}
		return instance;
	}

	public final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public final synchronized boolean initialize(final String searchPath, final String outputFile, final Pattern excludePattern, final Pattern includePattern, final long limitResults, final String metaDataSeperator, final boolean useRelativePath, final String icuFile) {
		if (!isInitialized) {
			this.searchPath = searchPath;
			this.outputFile = outputFile;
			this.excludePattern = excludePattern;
			this.includePattern = includePattern;
			this.resultLimit = limitResults;
			if (metaDataSeperator == null) {
				this.metaDataSeperator = constMetaDataSeperator;
			} else {
				this.metaDataSeperator = metaDataSeperator;
			}
			this.useRelativePath = useRelativePath;
			if (icuFile == null) {
				isInitialized = true;
			} else {
				transliterationInstance = TransliterationHelper.getInstance();
				if ( transliterationInstance.initialize(null, icuFile) ) {
					isInitialized = true;
				} else {
					transliterationInstance = null;
				}
			}
		}
		
		return isInitialized;
	}

	/**
	 * Configures debug data to be written when calling
	 * {@link scanDirectory()}.<br>
	 * {@code debugData} will be written at the beginning of {@code outputFile}.
	 * If {@code debugData} is not null a debug summary will be written at the
	 * end of {@code outputFile}.
	 * 
	 * @param debugData
	 *           debug data
	 */
	public final void setDebugData(final String debugData) {
		this.debugData = debugData;
	}

	private OutputStreamWriter writer = null;
	long docId = 0;
	long fileCount = 0;
	long dirCount = 0;
	long filesIndex = 0;
	long dirExcludeCount = 0;
	long fileExcludeCount = 0;
	long fileIncludeSkipCount = 0;

	/**
	 * Scans {@code searchPath} and writes to {@code outputFile}
	 */
	public final void scanDirectory() {
		writer = null;
		fileCount = 0;
		dirCount = 0;
		dirExcludeCount = 0;
		fileExcludeCount = 0;
		fileIncludeSkipCount = 0;
		if ((!isInitialized) || (searchPath == null) || (outputFile == null)) {
			System.err.println("SearchFiles is not properly initialized.");
			return;
		}
		writer = getWriter(outputFile);
		if (debugData != null) {
			try {
				writer.write(debugData);
			} catch (IOException e) {
				System.err.println("ERROR\tError writing to file (" + e.getMessage() + ").");
			}
		}
		walk(searchPath);

		try {
			writer.write("indexSize=" + docId + ";\n");
			if (debugData != null) {
				writer.write("/* " + dirExcludeCount + " directories excluded; " + dirCount + " directories; " + fileCount + " files; " + docId + " files in index; " + fileExcludeCount + " files excluded (ex); " + fileIncludeSkipCount + " files not included (in) */\n");
			}
		} catch (IOException e) {
			System.err.println("ERROR\tError writing to file (" + e.getMessage() + ").");
		}
		closeWriter(writer);
	}

	private final void walk(final String path) {
		if (docId == resultLimit) {
			System.out.println("WARNING\tSkipping files (index limit reached)!");
			// exit recursive / inner walk()
			return;
		}		File[] list = new File(path).listFiles();
		if (list == null) {
			return;
		}
		for (File f : list) {
			String absFileName = f.getAbsoluteFile().toString().replaceAll("(?sm)\\\\", "/");
			String relFileName = absFileName.replaceAll("^" + searchPath, ".");
			String fileName;
			if (useRelativePath) {
				fileName = relFileName;
			} else {
				fileName = absFileName;
			}
			if (f.isDirectory()) {
				dirCount++;
				if (excludePattern != null) {
					if (excludePattern.matcher(fileName).matches()) {
						dirExcludeCount++;
						System.out.println("INFO\tSkipping (excludeFilter): " + fileName);
						continue;
					}
				}
				walk(f.getAbsolutePath());
			} else {
				fileCount++;
				if (fileName.indexOf("\"") > -1) {
					fileExcludeCount++;
					System.out.println("WARNING\tSkipping (double quote): " + fileName);
					continue;
				}
				if (excludePattern != null) {
					if (excludePattern.matcher(fileName).matches()) {
						fileExcludeCount++;
						System.out.println("INFO\tSkipping (excludeFilter): " + fileName);
						continue;
					}
				}
				if (includePattern != null) {
					if (!includePattern.matcher(fileName).matches()) {
						fileIncludeSkipCount++;
						System.out.println("INFO\tSkipping (includeFilter): " + fileName);
						continue;
					}
				}

				// valid entry, write to disk			
				// 1st replaceAll: Separate file type from file name with space.
				// 2nd replaceAll: Separate words with spaces. Using the default metaDataSeperator 'A.B. C' remains 'A.B. C'.
				// 3nd replaceAll: Remove duplicate space characters.
				// 4th replaceAll: Replace '. ' with ' '
				String metaData = relFileName.replaceAll("^(.*)\\.([a-z0-9]*)$", "$1 $2").replaceAll(metaDataSeperator, " ").replaceAll("  *", " ").replaceAll("\\. ", " ");
				if (transliterationInstance != null) {
					metaData = dedupString(metaData + " " + transliterationInstance.transform(metaData));
				}
				try {
					if (metaData.charAt(0) == ' ') {
						metaData = metaData.substring(1);
					}
					if (metaData.charAt(metaData.length()-1) == ' ') {
						metaData = metaData.substring(0, metaData.length()-1);
					}
				} catch (Exception e) {}

				if (docId == resultLimit) {
					System.out.println("WARNING\tSkipping files (index limit reached)!");
					return;
				}
				try {
					docId++;
					writer.write("index.addDoc({'id':\"" + docId + "\",'metaData':\"" + metaData + "\",'fileName':\"" + fileName + "\"});\n");
				} catch (IOException e) {
					System.err.println("ERROR\tError writing to file (" + e.getMessage() + ").");
				}

			}
		}
	}

	/**
	 * @param s
	 *           String with words separated with space.
	 * @return String in random order with duplicate words removed.<br>
	 *         E.g.: {@code debugString("foo foo bar foo");} will return
	 *         {@code "bar foo"} or {@code "foo bar"}.
	 */
	private final String dedupString(final String s) {
		List<String> tmpList = Arrays.asList(s.split(" "));
		ArrayList<String> unique = new ArrayList<String>(new LinkedHashSet<String>(tmpList));
		String[] result = unique.toArray(new String[unique.size()]);
		return String.join(" ", result).replaceAll("  *", " ").substring(1);
	}

	private final OutputStreamWriter getWriter(final String outputFile) {
		try {
			return new OutputStreamWriter(new FileOutputStream(outputFile), constCharsetUTF8);
		} catch (FileNotFoundException e) {
			System.err.println("Could not open " + new File(outputFile).getAbsolutePath() + "(" + e.getMessage() + ")");
			System.exit(1);
		}
		return null;
	}

	private final void closeWriter(final OutputStreamWriter writer) {
		try {
			writer.close();
		} catch (IOException e) {
			System.err.println("Could not close file. (" + e.getMessage() + ")");
			System.exit(1);
		}
	}
}
