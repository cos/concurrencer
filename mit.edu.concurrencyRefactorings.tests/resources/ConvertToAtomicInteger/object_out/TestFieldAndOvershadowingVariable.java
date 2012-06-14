package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestFieldAndOvershadowingVariable {

	AtomicInteger f = new AtomicInteger();

	void incrementByAdding() {
		f.addAndGet(2);
		int f = 0;
		f = f + 12;
	}
}
