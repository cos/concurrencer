package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestReturnType {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	HashMap hm2 = new HashMap();

	public ConcurrentHashMap doReturn() {
		return hm;
	}
	
	public HashMap doReturn2() {
		return hm2;
	}
	
	public Map doReturn3() {
		return hm;
	}
}