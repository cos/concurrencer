package object_in;

import java.util.HashMap;

public class TestAfterIfUsesCreatedValue {
	
	private static final Object CACHE_ATTR = null;
	private Object _sourceResolver;
	private HashMap appScope = new HashMap();

	public Object getAnnotationReader() {
		Object cache = appScope.get(CACHE_ATTR);
		if (cache == null) {
			cache = "";
			appScope.put(CACHE_ATTR, cache);
		}
		return cache;
	}

	public Object getAnnotationReader2() {
		Object cache2 = appScope.get(CACHE_ATTR);
		if (cache2 == null) {
			cache2 = "";
			int b = 2;
			appScope.put(CACHE_ATTR, cache2);
		}
		return cache2;
	}

	public void getAnnotationReader3() {
		int a = 1;
		int b = 2;
		Object cache3 = appScope.get(CACHE_ATTR);
		if (cache3 == null) {
			int c = 3;
			cache3 = "";
			appScope.put(CACHE_ATTR, cache3);
			int d = 4;
		}
		cache3.toString();
		cache3.hashCode();
	}
}