package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPreserveFormAndOrdering {
	
	private static HashMap _classCaches = new HashMap();

	public static void getCache0000() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			_classCaches.put("a_key", "");
		}
	}

	public static void getCache0001() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			_classCaches.put("a_key", "");
		}
		
		int i = 7;
		int j = 8;
	}

	public static void getCache0010() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			_classCaches.put("a_key", "");
			int g = 5;
			int h = 6;
		}
	}

	public static void getCache0011() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			_classCaches.put("a_key", "");
			int g = 5;
			int h = 6;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache0100() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			_classCaches.put("a_key", "");
		}
	}
	
	public static void getCache0100b() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			cache = "";
			_classCaches.put("a_key", cache);
		}
	}

	public static void getCache0101() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			_classCaches.put("a_key", "");
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache0110() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			_classCaches.put("a_key", "");
			int g = 5;
			int h = 6;
		}
	}

	public static void getCache0111() {
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			_classCaches.put("a_key", "");
			int g = 5;
			int h = 6;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache1000() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			_classCaches.put("a_key", "");
		}
	}

	public static void getCache1001() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			_classCaches.put("a_key", "");
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache1010() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			_classCaches.put("a_key", "");
			int g = 5;
			int h = 6;
		}
	}

	public static void getCache1011() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			_classCaches.put("a_key", "");
			int g = 5;
			int h = 6;
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache1100() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			_classCaches.put("a_key", "");
		}
	}

	public static void getCache1101() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			_classCaches.put("a_key", "");
		}

		int i = 7;
		int j = 8;
	}

	public static void getCache1110() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			_classCaches.put("a_key", "");
			int g = 5;
			int h = 6;
		}
	}

	public static void getCache1111() {
		int a = 0;
		int b = 1;
		String cache = (String) _classCaches.get("a_key");

		if(cache == null) {
			int e = 4;
			int f = 5;
			_classCaches.put("a_key", "");
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

		if(cache == null) {
			int e = 4;
			int f = 5;
			cache = "";
			_classCaches.put("a_key", cache);
			int g = 5;
			int h = 6;
		}
		int i = 7;
		int j = 8;
	}
}