package object_out;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestMapInitialization {
	
	private static final Object CACHE_ATTR = null;
	private Object _sourceResolver;
	private ConcurrentHashMap appScope = new ConcurrentHashMap();

	public void getAnnotationReader(Class type) {
		appScope.putIfAbsent(CACHE_ATTR, "");
	}
}