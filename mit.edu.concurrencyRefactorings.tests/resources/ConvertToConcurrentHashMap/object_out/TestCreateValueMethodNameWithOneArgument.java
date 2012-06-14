package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class C3 {
	ConcurrentHashMap map2 = new ConcurrentHashMap();
	
	void doPutIfAbsentBlockWithExtractCreateValue() {
		String something = null;
		map2.putIfAbsent("a_key", createaValue(something));
	}

	private String createaValue(String something) {
		String aValue = null;
		if (map2.isEmpty()) {
			aValue = something;
		} else { 
			aValue = "aValue";
		}
		return aValue;
	}
}