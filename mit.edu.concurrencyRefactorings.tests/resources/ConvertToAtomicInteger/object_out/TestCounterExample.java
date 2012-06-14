package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public static class TestCounterExample {
    AtomicInteger value = new AtomicInteger(0);
    
    public int getCounter() {
        return value.get();
    }
    
    public void setCounter(int counter) {
        this.value.set(counter);
    }
    
    public int inc() {
        return value.incrementAndGet();
    }
}