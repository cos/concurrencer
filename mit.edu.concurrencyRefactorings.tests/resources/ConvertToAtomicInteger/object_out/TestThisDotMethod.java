package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestThisDotMethod {

	AtomicInteger f = new AtomicInteger();

	public void setCounter(int value) {
        this.f.set(value);
    }
}