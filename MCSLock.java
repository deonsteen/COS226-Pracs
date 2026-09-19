public class MCSLock {
import java.util.concurrent.atomic.AtomicReference;

    public class MCSLock implements Lock {

        private static class QNode {
            volatile boolean locked = false;
            volatile QNode next = null;
        }

        // Tail of the queue. null means the lock is currently free.
        private final AtomicReference<QNode> tail = new AtomicReference<>(null);

        // Each thread needs its own node to track its place in the queue.
        // ThreadLocal means thread A's node never gets mixed up with thread B's.
        private final ThreadLocal<QNode> myNode = ThreadLocal.withInitial(QNode::new);

        @Override
        public void lock() {
            QNode qnode = myNode.get();
            qnode.next = null;    // reset in case this thread's node is being reused
            qnode.locked = true;  // assume we'll have to wait, until we know otherwise

            QNode predecessor = tail.getAndSet(qnode); // atomically join the back of the queue

            if (predecessor != null) {
                // Someone was already ahead of us in the queue - link ourselves in
                predecessor.next = qnode;

                // Spin on OUR OWN node's field, not a shared variable -
                // this is the whole point of MCS: no contention on one hot memory location.
                while (qnode.locked) {
                    // Busy-wait
                }
            }
            // Else: queue was empty, we grabbed the lock immediately.
        }

        @Override
        public void unlock() {
            QNode qnode = myNode.get();

            if (qnode.next == null) {
                // We don't know of a successor yet - try to mark the queue as empty.
                if (tail.compareAndSet(qnode, null)) {
                    return; // Succeeded: we really were last in line.
                }
                // CAS failed: another thread is mid-way through enqueuing behind us.
                // Wait for it to finish setting predecessor.next = qnode.
                while (qnode.next == null) {
                    // Busy-wait
                }
            }

            // Hand the lock off directly to our successor.
            qnode.next.locked = false;
            qnode.next = null; // help GC, reset for reuse
        }
    }
}

