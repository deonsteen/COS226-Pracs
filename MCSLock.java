
import java.util.concurrent.atomic.AtomicReference;

    public class MCSLock implements Lock {

        private static class QNode {
            volatile boolean locked = false;
            volatile QNode next = null;
        }

        // Tail; (null means the lock is currently free)
        private final AtomicReference<QNode> tail = new AtomicReference<>(null);

        // Each thread gets its own node to track its pos
        // ThreadLocal means thread A's node never gets mixed up with thread B's
        private final ThreadLocal<QNode> myNode = ThreadLocal.withInitial(QNode::new);

        @Override
        public void lock() {
            QNode qnode = myNode.get();
            qnode.next = null;    // reset in case this threads node is being reused
            qnode.locked = true;  // assume well have to wait

            QNode predecessor = tail.getAndSet(qnode); // atomically join at the back

            if (predecessor != null) {
                // if someone was already ahead of us in the que
                predecessor.next = qnode;

                while (qnode.locked) {
                    // Busy-wait
                }
            }
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


