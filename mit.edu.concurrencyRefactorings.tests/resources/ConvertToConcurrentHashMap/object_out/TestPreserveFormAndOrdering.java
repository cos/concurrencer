package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestPreserveFormAndOrdering {
	
	private static ConcurrentHashMap _classCaches = new ConcurrentHashMap();

	public static void getCache0000() {
		_classCaches.putIfAbsent("a_key", "");
	}

	public static void getCache0001() {
		_classCaches.putIfAbsent("a_key", "");
		
		int i = 7;
		int j = 8;
	}

	public static void getCache0010() {
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int g = 5;
			int h = 6;
		}
	}

	public static void getCache0011() {
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int g = 5;
			int h = 6;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache0100() {
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int e = 4;
			int f = 5;
		}
	}
	
	public static void getCache0100b() {
		String cache = (String) _classCaches.get("a_key");
		String createdcache = createcache();
		if (_classCaches.putIfAbsent("a_key", createdcache) == null) {
			cache = createdcache;
		}
	}

	private static String createcache() {
		String cache;
		int e = 4;
		int f = 5;
		cache = "";
		return cache;
	}

	public static void getCache0101() {
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int e = 4;
			int f = 5;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache0110() {
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int e = 4;
			int f = 5;
			int g = 5;
			int h = 6;
		}
	}

	public static void getCache0111() {
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int e = 4;
			int f = 5;
			int g = 5;
			int h = 6;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache1000() {
		int a = 0;
		int b = 1;
		_classCaches.putIfAbsent("a_key", "");
	}

	public static void getCache1001() {
		int a = 0;
		int b = 1;
		_classCaches.putIfAbsent("a_key", "");

		int i = 7;
		int j = 8;
	}

	public static void getCache1010() {
		int a = 0;
		int b = 1;
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int g = 5;
			int h = 6;
		}
	}

	public static void getCache1011() {
		int a = 0;
		int b = 1;
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int g = 5;
			int h = 6;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache1100() {
		int a = 0;
		int b = 1;
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int e = 4;
			int f = 5;
		}
	}

	public static void getCache1101() {
		int a = 0;
		int b = 1;
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int e = 4;
			int f = 5;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache1110() {
		int a = 0;
		int b = 1;
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int e = 4;
			int f = 5;
			int g = 5;
			int h = 6;
		}
	}

	public static void getCache1111() {
		int a = 0;
		int b = 1;
		if (_classCaches.putIfAbsent("a_key", "") == null) {
			int e = 4;
			int f = 5;
			int g = 5;
			int h = 6;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache1111b() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");
		String createdcache = createcache();
		if (_classCaches.putIfAbsent("a_key", createdcache) == null) {
			cache = createdcache;
			int g = 5;
			int h = 6;
		}
		int i = 7;
		int j = 8;
	}

	private static String createcache() {
		String cache;
		int e = 4;
		int f = 5;
		cache = "";
		return cache;
	}
}