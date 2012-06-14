package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestStatementsBeforePut_NoCreateValue {
	
	public HashMap hm = new HashMap();
	private static final String THE_KEY = "a_key";
	
	public void doBeforePutNoCreateValue() {
		if(hm.containsKey(THE_KEY)) {
			System.out.println(THE_KEY);
			hm.put(THE_KEY, "a_value");
		}
	}
}