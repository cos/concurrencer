package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestStatementsBeforePut_NeverUsed {
	
	HashMap hm = new HashMap();
	
	public void doBeforeAndAfterPut() {
		if(hm.containsKey("a_key")) {
			String value = new String(" this is a value ");
			hm.put("a_key", "");
		}
	}
}