package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestDecrementPostfix {

	AtomicInteger f = new AtomicInteger();

	void decrementPostfix() {
		f.getAndDecrement();
	}
}