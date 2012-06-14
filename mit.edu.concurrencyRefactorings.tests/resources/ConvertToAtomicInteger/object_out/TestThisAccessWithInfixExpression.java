package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestThisAccessWithInfixExpression {

	AtomicInteger f = new AtomicInteger();

	void doAccess() {
		this.f.addAndGet(5);
	}
}