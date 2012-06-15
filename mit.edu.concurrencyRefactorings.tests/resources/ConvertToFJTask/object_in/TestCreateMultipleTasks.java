package object_in;

public class TestCreateMultipleTasks {
	
	public int method(int num) {
		if (num <= 0) {
			return 0;
		} else {
			int i = method(num - 1);
			int j = method(num - 2);
			int k = method(num - 3);
			return i + j + k;
		}
	}
}