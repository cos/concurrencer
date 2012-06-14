package object_in;

public class TestSuperDotMethod {

	int f;
}

class SuperDotMethod extends TestSuperDotMethod {

	public synchronized void setCounter(int value) {
		super.f = value;
    }
}