package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestReimplementRecursiveMethod {
	
	public void method(int[] array) {
		ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		Classmethod aClassmethod = new Classmethod(array);
		pool.invoke(aClassmethod);
	}

	public class Classmethod extends RecursiveAction {
	}
}