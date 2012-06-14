package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestCastingOnGet {
	
	private static HashMap _classCaches = new HashMap();

	public static void getCache() {
		String cache = (String) _classCaches.get("a_key");

		if (cache == null) {
			_classCaches.put("a_key", "a_value");
		}
	}
}