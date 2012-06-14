package object_in;

import jsr166y.forkjoin.ForkJoinExecutor;
import jsr166y.forkjoin.ForkJoinPool;
import jsr166y.forkjoin.RecursiveAction;

public class TestReimplementRecursiveMethod {
	
	public void method(int[] array) {
		ForkJoinExecutor pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		Classmethod aClassmethod = new Classmethod(array);
		pool.invoke(aClassmethod);
	}

	public class Classmethod extends RecursiveAction {
	}
}