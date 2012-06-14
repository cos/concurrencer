package object_out;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestPreserveMap {
	
	private static final Object CACHE_ATTR = null;
	private Object _sourceResolver;
	private Map appScope = new ConcurrentHashMap();

	public void getAnnotationReader(Class type) {
		appScope.clear();
	}
}