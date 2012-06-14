package object_out;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestInitializeInConstructor {
	
	ConcurrentHashMap hm;
	
	public TestInitializeInConstructor() {
		hm = new ConcurrentHashMap();
	}
}