package object_in;

public class TestReimplementRecursiveMethod {
	
	public void method(int[] array) {
		if (array.length == 0) {
			return;
		}
		else {
			method(array);
			method(new int[] {1,2,3});
		}
	}
}