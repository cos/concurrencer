package object_in;

public class TestSynchronizedMethodMultipleAccess {

	int f;

	synchronized void syncMultipleAccess() {
		f = 12;
		f++;
	}
}