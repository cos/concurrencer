package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestIncrementByAdding {

	AtomicInteger f = new AtomicInteger();

	void incrementByAdding() {
		f.addAndGet(12);
	}
}