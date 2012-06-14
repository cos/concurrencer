package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestMultipleFields {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	HashMap hm2 = new HashMap();

	void doMultipleFields() {
		hm.clear();
		hm2.clear();
	}
}