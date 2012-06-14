package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestDecrementPrefix {

	AtomicInteger f = new AtomicInteger();

	void decrementPrefix() {
		f.decrementAndGet();
	}
}