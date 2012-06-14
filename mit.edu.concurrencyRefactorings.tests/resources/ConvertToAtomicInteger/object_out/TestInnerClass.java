package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestInnerClass {

	class innerClass {
        AtomicInteger f = new AtomicInteger();
        
        public void inc() {
        	f.getAndIncrement();
        }
        
        private void returnValue() {
        	return f.get();
        }
        
        private void set() {
        	f.set(12);
        }
        
        public void syncBlock() {
        	f.getAndIncrement();
        }
        
        protected void syncMethod() {
        	f.getAndIncrement();
        }
	}
}