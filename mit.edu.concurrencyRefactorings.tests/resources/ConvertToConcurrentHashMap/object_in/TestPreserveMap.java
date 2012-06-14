package object_in;

import java.util.HashMap;
import java.util.Map;

public class TestPreserveMap {
	
	private static final Object CACHE_ATTR = null;
	private Object _sourceResolver;
	private Map appScope = new HashMap();

	public void getAnnotationReader(Class type) {
		appScope.clear();
	}
}