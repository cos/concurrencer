package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestAfterIfUsesCreatedValue {
	
	private static final Object CACHE_ATTR = null;
	private Object _sourceResolver;
	private ConcurrentHashMap appScope = new ConcurrentHashMap();

	public Object getAnnotationReader() {
		Object cache = appScope.get(CACHE_ATTR);
		Object createdcache = "";
		if (appScope.putIfAbsent(CACHE_ATTR, createdcache) == null) {
			cache = createdcache;
		}
		return cache;
	}

	public Object getAnnotationReader2() {
		Object cache2 = appScope.get(CACHE_ATTR);
		Object createdcache2 = createcache2();
		if (appScope.putIfAbsent(CACHE_ATTR, createdcache2) == null) {
			cache2 = createdcache2;
		}
		return cache2;
	}

	private Object createcache2() {
		Object cache2;
		cache2 = "";
		int b = 2;
		return cache2;
	}

	public void getAnnotationReader3() {
		int a = 1;
		int b = 2;
		Object cache3 = appScope.get(CACHE_ATTR);
		Object createdcache3 = createcache3();
		if (appScope.putIfAbsent(CACHE_ATTR, createdcache3) == null) {
			cache3 = createdcache3;
			int d = 4;
		}
		cache3.toString();
		cache3.hashCode();
	}

	private Object createcache3() {
		Object cache3;
		int c = 3;
		cache3 = "";
		return cache3;
	}
}