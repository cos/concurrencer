package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestLeaveAfterPattern {
	
	private static HashMap _classCaches = new HashMap();

	public static void getCache() {
		String cache = (String) _classCaches.get("a_key");

		if (cache == null) {
			cache = "";
			_classCaches.put("a_key", cache);
		}

		int b = 2;
	}
	
	public static void getCache2() {
		String cache = (String) _classCaches.get("a_key");

		if (cache == null) {
			_classCaches.put("a_key", "");
		}

		int b = 2;
	}
	
	public static void getCache3() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			cache = "";
			_classCaches.put("a_key", cache);
			int c = 2;
		}

		int b = 2;
	}
}