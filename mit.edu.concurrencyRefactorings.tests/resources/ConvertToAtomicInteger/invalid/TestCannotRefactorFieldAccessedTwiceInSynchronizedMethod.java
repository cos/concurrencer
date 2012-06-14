package invalid;

public class TestCannotRefactorFieldAccessedTwiceInSynchronizedMethod {

	int f;

	synchronized void twoFieldsInSyncMethod() {
		f = f - 12;
		f++;
	}
}
