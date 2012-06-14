package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestSuperDotMethod {

	AtomicInteger f = new AtomicInteger();
}

class SuperDotMethod extends TestSuperDotMethod {

	public void setCounter(int value) {
		super.f.set(value);
    }
}