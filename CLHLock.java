import java.util.concurrent.atomic.AtomicReference;

public class CLHLock implements Lock
{
    private static class QNode
    {
        volatile boolean locked = false;
    }
    //test
    //Tail of the implicit queue. Starts with a dummy unlocked node so the first
    //thread to call lock() sees an unlocked predecessor and returns immediately.
    private final AtomicReference<QNode> tail = new AtomicReference<>(new QNode());

    //Each thread needs its own "current node" and "predecessor node" that persist
    //between lock() and unlock() calls, without being visible to other threads.
    private final ThreadLocal<QNode> myNode = ThreadLocal.withInitial(QNode::new);
    private final ThreadLocal<QNode> myPred = new ThreadLocal<>();

    @Override
    public void lock()
    {
        QNode qnode = myNode.get();
        qnode.locked = true;

        //Atomically swap myself in as the new tail and grab whoever was there before.
        QNode pred = tail.getAndSet(qnode);
        myPred.set(pred);

        //Spin on my predecessor's node (not a shared global), so each waiting
        //thread spins on a different memory location.
        while (pred.locked)
        {
            //Busy-wait
        }
    }

    @Override
    public void unlock()
    {
        QNode qnode = myNode.get();
        qnode.locked = false;

        //Recycle my predecessor's node as my node for next time, since it is
        //now free for someone else's successor to spin on.
        myNode.set(myPred.get());
    }
}
