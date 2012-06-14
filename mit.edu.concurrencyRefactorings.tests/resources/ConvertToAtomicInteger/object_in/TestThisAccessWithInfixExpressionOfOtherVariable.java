package object_in;

public class TestThisAccessWithInfixExpressionOfOtherVariable {

	int f;

	void doAccess(int value) {
		this.f = value + value;
	}
}