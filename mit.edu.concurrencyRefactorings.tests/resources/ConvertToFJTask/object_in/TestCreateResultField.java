package object_in;

public class TestCreateResultFied {
	
	public int method(int[] array, int start, int end) {
		if (array.length == 0) {
			return 0;
		}
		else {
			int i = method(array, 0, 1);
			int j = method(new int[] {1,2,3}, 0, 3);
			return i + j;
		}
	}
}