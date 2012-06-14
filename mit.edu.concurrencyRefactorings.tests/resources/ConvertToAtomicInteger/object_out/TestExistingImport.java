package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestExistingImport {

	AtomicInteger f = new AtomicInteger();

	void inc() {
		f.getAndIncrement();
	}
}