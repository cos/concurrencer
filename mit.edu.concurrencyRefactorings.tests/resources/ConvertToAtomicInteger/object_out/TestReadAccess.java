package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestReadAccess {
	
	AtomicInteger field = new AtomicInteger();
	
	int readAccess() {
		return field.get();
	}

}