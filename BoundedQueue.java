
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedQueue<T> implements ConcurrentQueue<T> {

    private final Object[] items;
    private final int capacity;

    private int head = 0;
    private int tail = 0;
    private int count = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public BoundedQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                "Capacity must be greater than zero"
            );
        }

        this.capacity = capacity;
        this.items = new Object[capacity];
    }

    @Override
    public void enq(T item) {
        if (item == null) {
            throw new IllegalArgumentException(
                "Null items are not allowed"
            );
        }

        lock.lock();
        try {
            while (count == capacity) {
                notFull.awaitUninterruptibly();
            }

            items[tail] = item;
            tail = (tail + 1) % capacity;
            count++;

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deq() {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.awaitUninterruptibly();
            }

            T item = (T) items[head];
            items[head] = null;

            head = (head + 1) % capacity;
            count--;

            notFull.signal();

            return item;
        } finally {
            lock.unlock();
        }
    }
}
