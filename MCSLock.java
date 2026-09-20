import java.util.concurrent.atomic.AtomicReference;

public class MCSLock implements Lock {

    private static class QNode {
        volatile boolean locked = false;
        volatile QNode next = null;
    }

    private final AtomicReference<QNode> tail = new AtomicReference<>(null);
    private final ThreadLocal<QNode> myNode = new ThreadLocal<>();

    @Override
    public void lock() {
        QNode qnode = new QNode(); // fresh node every call - avoids reuse races
        qnode.locked = true;
        myNode.set(qnode);

        QNode predecessor = tail.getAndSet(qnode);

        if (predecessor != null) {
            predecessor.next = qnode;
            while (qnode.locked) {
                Thread.yield();
            }
        }
    }

    @Override
    public void unlock() {
        QNode qnode = myNode.get();

        if (qnode.next == null) {
            if (tail.compareAndSet(qnode, null)) {
                return;
            }
            while (qnode.next == null) {
                // busy-wait
            }
        }

        qnode.next.locked = false;
        qnode.next = null;
    }
}
