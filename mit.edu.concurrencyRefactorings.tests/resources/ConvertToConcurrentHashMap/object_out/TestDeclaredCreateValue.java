package object_out;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestDeclaredCreateValue {
	
	ConcurrentHashMap map = new ConcurrentHashMap();

	void doPutIfAbsentBlockWithCreateValueOneStatement(String hi, HashMap<String, Integer> hello) {
		map.putIfAbsent("a_key", "aValue!");
	}
}