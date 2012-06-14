package object_in;

import java.util.HashMap;
import java.util.Map;

public class TestMapInitialization {
	
	private static final Object CACHE_ATTR = null;
	private Object _sourceResolver;
	private Map appScope = new HashMap();

	public void getAnnotationReader(Class type) {
		Object cache = appScope.get(CACHE_ATTR);

		if (cache == null) {
			cache = "";
			appScope.put(CACHE_ATTR, cache);
		}
	}
}