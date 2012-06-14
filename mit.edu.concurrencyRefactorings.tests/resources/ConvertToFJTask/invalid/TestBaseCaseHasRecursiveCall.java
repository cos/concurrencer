package invalid;

public class TestBaseCaseHasRecursiveCall {
	
	public void method(int[] array) {
		if (array.length == 0) {
			array = null;
			method(array);
			return;
		}
		else {
			method(array);
			method(new int[] {1,2,3});
		}
	}
}