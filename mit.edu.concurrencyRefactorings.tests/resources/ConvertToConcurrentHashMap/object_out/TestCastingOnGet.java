package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestCastingOnGet {
	
	private static ConcurrentHashMap _classCaches = new ConcurrentHashMap();

	public static void getCache() {
		_classCaches.putIfAbsent("a_key", "a_value");
	}
}