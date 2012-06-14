package invalid;

public class TestCannotRefactorTwoFieldsInSynchronizedMethod {

	int f;
	int g;

	synchronized void twoFieldsInSyncMethod() {
		f = f + 12;
		g = g + 3;
	}
}
