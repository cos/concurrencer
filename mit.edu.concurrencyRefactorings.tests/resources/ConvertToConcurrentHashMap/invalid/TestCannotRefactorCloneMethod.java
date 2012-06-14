package invalid;

import java.util.HashMap;

public class TestCannotRefactorCloneMethod {
	
	HashMap hm = new HashMap();

	void doClone() {
		hm.clone();
	}
}