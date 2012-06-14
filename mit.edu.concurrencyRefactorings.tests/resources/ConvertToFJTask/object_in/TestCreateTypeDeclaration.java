package object_in;

public class CreateTypeDeclaration {
	
	public void method(int[] array, int start, int end) {
		if (array.length == 0) {
			return;
		}
		else {
			method(array, 0, 1);
			method(new int[]{1, 2, 3}, 0, 3);
		}
	}
}