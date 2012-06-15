package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestCreateResultField {
	
	public int method(int[] array, int start, int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MethodImpl aMethodImpl = new MethodImpl(array, start, end);
		pool.invoke(aMethodImpl);
		return aMethodImpl.result;
	}

	public class MethodImpl extends RecursiveAction {
		private int[] array;
		private int start;
		private int end;
		private int result;
		private MethodImpl(int[] array, int start, int end) {
			this.array = array;
			this.start = start;
			this.end = end;
		}
		protected void compute() {
			if ((array.length < 10)) {
				result = method(array, start, end);
				return;
			} else {
				MethodImpl task1 = new MethodImpl(array, 0, 1);
				MethodImpl task2 = new MethodImpl(new int[]{1, 2, 3}, 0, 3);
				invokeAll(task1, task2);
				int i = task1.result;
				int j = task2.result;
				result = i + j;
			}
		}
		public int method(int[] array, int start, int end) {
			if (array.length == 0) {
				return 0;
			} else {
				int i = method(array, 0, 1);
				int j = method(new int[]{1, 2, 3}, 0, 3);
				return i + j;
			}
		}
	}
}