// Shared interface for both queues (agreed by the whole group - don't change without telling Danel & Logan)
public interface ConcurrentQueue<T>
{
    void enq(T item);

    // BoundedQueue: blocks until an item is available.
    // LockFreeQueue: returns null immediately if the queue is empty.
    T deq();
}
