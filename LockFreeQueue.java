import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> implements ConcurrentQueue<T> {
    // Private inner Node so it won't clash with anyone else's Node.java
    private static class Node<T> {
        final T value;
        final AtomicReference<Node<T>> next;

        Node(T value) {
            this.value = value;
            this.next = new AtomicReference<>(null);
        }
    }

    private final AtomicReference<Node<T>> head;
    private final AtomicReference<Node<T>> tail;

    public LockFreeQueue() {
        Node<T> sentinel = new Node<>(null);
        head = new AtomicReference<>(sentinel);
        tail = new AtomicReference<>(sentinel);
    }

    @Override
    public void enq(T item) {
        Node<T> holdItemNode = new Node<>(item);

        while (true) {
            Node<T> last = tail.get();
            Node<T> next = last.next.get();

            if (tail.get() == last) {
                if (next == null) {
                    if (last.next.compareAndSet(next, holdItemNode)) {
                        tail.compareAndSet(last, holdItemNode);
                        return;
                    }
                } else {
                    tail.compareAndSet(last, next);
                }
            }

        }
    }

    @Override
    public T deq() {
        while (true) {
            Node<T> first = head.get();
            Node<T> last = tail.get();
            Node<T> next = first.next.get();

            if (head.get() == first) {

                if (first == last) {
                    if (next == null) {
                        return null;
                    }
                    tail.compareAndSet(last, next);

                } else {
                    T value = next.value;
                    if (head.compareAndSet(first, next)) {
                        return value;
                    }
                }
            }

        }
    }
}
