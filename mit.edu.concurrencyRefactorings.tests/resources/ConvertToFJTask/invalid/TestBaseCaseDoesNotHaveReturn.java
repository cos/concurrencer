package invalid;

public class TestBaseCaseDoesNotHaveReturn {
	
	public void method(int[] array) {
		if (array.length == 0) {
			array = null;
		}
		else {
			method(array);
			method(new int[] {1,2,3});
		}
	}
}