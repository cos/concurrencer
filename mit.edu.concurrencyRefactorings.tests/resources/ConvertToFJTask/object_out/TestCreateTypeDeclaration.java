package object_out;

import jsr166y.forkjoin.ForkJoinExecutor;
import jsr166y.forkjoin.ForkJoinPool;
import jsr166y.forkjoin.RecursiveAction;

public class CreateTypeDeclaration {
	
	public void method(int[] array, int start, int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinExecutor pool = new ForkJoinPool(processorCount);
		MethodImpl aMethodImpl = new MethodImpl(array, start, end);
		pool.invoke(aMethodImpl);
	}

	public class MethodImpl extends RecursiveAction {
		private int[] array;
		private int start;
		private int end;
		private MethodImpl(int[] array, int start, int end) {
			this.array = array;
			this.start = start;
			this.end = end;
		}
		protected void compute() {
			if ((array.length < 10)) {
				method(array, start, end);
				return;
			} else {
				MethodImpl task1 = new MethodImpl(array, 0, 1);
				MethodImpl task2 = new MethodImpl(new int[]{1, 2, 3}, 0, 3);
				forkJoin(task1, task2);
			}
		}
		public void method(int[] array, int start, int end) {
			if (array.length == 0) {
				return;
			} else {
				method(array, 0, 1);
				method(new int[]{1, 2, 3}, 0, 3);
			}
		}
	}
}