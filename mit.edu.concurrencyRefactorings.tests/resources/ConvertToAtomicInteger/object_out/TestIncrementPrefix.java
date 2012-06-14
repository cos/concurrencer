package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestIncrementPrefix {

	AtomicInteger f = new AtomicInteger();

	void incrementPrefix() {
		f.incrementAndGet();
	}
}