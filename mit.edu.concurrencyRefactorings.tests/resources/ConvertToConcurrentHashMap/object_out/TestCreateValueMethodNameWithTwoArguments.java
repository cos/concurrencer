package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestCreateValueMethodNameWithTwoArguments {
	ConcurrentHashMap map2 = new ConcurrentHashMap();
	
	void doPutIfAbsentBlockWithExtractCreateValue() {
		String something = null;
		String another = null;
		map2.putIfAbsent("a_key", createaValue(something, another));
	}

	private String createaValue(String something, String another) {
		String aValue = null;
		if (map2.isEmpty()) {
			aValue = something;
		} else { 
			aValue = another;
		}
		return aValue;
	}
}