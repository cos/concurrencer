package object_in;

public static class TestCounterExample {
    int value = 0;
    
    public int getCounter() {
        return value;
    }
    
    public synchronized void setCounter(int counter) {
        this.value = counter;
    }
    
    public synchronized int inc() {
        return ++value;
    }
}