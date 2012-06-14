package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingContainsKey_WithCreateValue {
	
	ConcurrentHashMap map = new ConcurrentHashMap();
	
	void doPutIfAbsentBlockWithCreateValueOneStatement() {
		map.putIfAbsent("a_key", "aValue");
	}

	void doPutIfAbsentBlockWithExtractCreateValue() {
		map.putIfAbsent("a_key", createaValue());
	}

	private String createaValue() {
		String aValue = null;
		if (map.isEmpty()) {
			aValue = "empty";
		} else { 
			aValue = "aValue";
		}
		return aValue;
	}
}