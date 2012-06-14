package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestIncrementPostfix {

	AtomicInteger f = new AtomicInteger();

	void incrementPostfix() {
		f.getAndIncrement();
	}
}