package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestLeaveAfterPattern {
	
	private static ConcurrentHashMap _classCaches = new ConcurrentHashMap();

	public static void getCache() {
		_classCaches.putIfAbsent("a_key", "");

		int b = 2;
	}
	
	public static void getCache2() {
		_classCaches.putIfAbsent("a_key", "");

		int b = 2;
	}
	
	public static void getCache3() {
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int c = 2;
		}

		int b = 2;
	}
}