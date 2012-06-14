package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestStatementsBeforeAndAfterPut_AfterDoesNotUseCreateValue {
	
	HashMap hm = new HashMap();
	
	public void doBeforeAndAfterPut() {
		if (hm.containsKey("a_key")) {
			String value = new String(" this is a value ");
			value = "";
			hm.put("a_key", value);
			System.out.println("a_key");
		}
	}
}