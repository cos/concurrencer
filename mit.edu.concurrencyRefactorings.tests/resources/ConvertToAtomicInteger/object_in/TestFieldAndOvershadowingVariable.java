package object_in;

public class TestFieldAndOvershadowingVariable {

	int f;

	void incrementByAdding() {
		f = f + 2;
		int f = 0;
		f = f + 12;
	}
}
