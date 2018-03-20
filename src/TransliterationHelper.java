import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This class implements<br>
 * {@code import com.ibm.icu.text.Transliterator;}<br>
 * {@code t = Transliterator.getInstance(transliteratorId);}<br>
 * {@code s = t.transform(stringToTransliterate);}<br>
 * without the need to add {@code icuFile} to the class path.
 * {@code icuFile} is a huge file (10 MB) and loading may be avoided on slow systems.<br>
 * It is only testd with the <a href="http://download.icu-project.org/files/icu4j/60.2/#icu4j-60_2.jar">http://download.icu-project.org/files/icu4j/60.2/</a> icu4j-60_2.jar version!
 */
public class TransliterationHelper {
	private final static String constIcuClass = "com.ibm.icu.text.Transliterator";
	public final static String constTransliteratorId = "Any-Latin; NFD; [^\\ \\p{Alnum}] Remove";
	private static TransliterationHelper instance = null;
	private boolean isInitialized = false;

	private TransliterationHelper() {	
	}
	public final static synchronized TransliterationHelper getInstance() {
		if (instance == null) {
			instance = new TransliterationHelper();
		}
		return instance;
	}
	public final Object clone() throws CloneNotSupportedException {
	    throw new CloneNotSupportedException();
	}
	
	public final synchronized boolean initialize(final String transliteratorId, final String icuFile) {
		if (!isInitialized) {
			try {
				if (transliteratorId == null) {
					enableTransliteration(constTransliteratorId, icuFile);
				} else {
					enableTransliteration(transliteratorId, icuFile);
				}
				isInitialized = true;
			} catch (Exception e) {
				System.err.println("WARNING\tCould not load transliteration library '" + icuFile + "'.");
			}
		}
		return isInitialized;
	}
	
	private Object transliterator = null;
	private Method transformMethod = null;
	private final void enableTransliteration(final String transliteratorId, final String icuFile) throws Exception {
		// src: https://cvamshi.wordpress.com/2011/01/12/loading-jars-and-java-classes-dynamically/
		File file = new File (icuFile);
		URL url = file.toURI().toURL();
		URL[] urls = new URL[] {url};
		URLClassLoader cl = URLClassLoader.newInstance(urls);	
		Class<?> transliteratorClass = cl.loadClass(constIcuClass);

		Method getInstanceMethod = transliteratorClass.getMethod("getInstance", new Class[] {String.class});
		/* Transliterator */ transliterator = getInstanceMethod.invoke(transliteratorClass, transliteratorId);
		
		/* Method */ transformMethod = transliteratorClass.getMethod("transform", new Class[] {String.class});
	}
	
	public final String transform(final String s) {
		if (transformMethod == null) {
			return s;
		}
		String newS = null;
		try {
			newS = (String) transformMethod.invoke(transliterator, s);
		} catch (Exception e) {
			System.err.println("ERROR\tCould not transform (" + e.getMessage() + ").");
		}
		return newS;
	}
}
