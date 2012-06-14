package object_in;

public class TestInnerClass {

	class innerClass {
        int f;
        
        public void inc() {
        	f++;
        }
        
        private void returnValue() {
        	return f;
        }
        
        private void set() {
        	f = 12;
        }
        
        public void syncBlock() {
        	synchronized(this) {
        		f++;
        	}
        }
        
        protected synchronized void syncMethod() {
        	f++;
        }
	}
}