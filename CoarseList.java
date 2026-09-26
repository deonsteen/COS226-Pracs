import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseList 
{

    private final Node head;
    private final Node tail;

    private final Lock lock = new ReentrantLock();

    public CoarseList() 
    {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);

        head.next = tail;
    }

    public boolean add(int value) 
    {
        lock.lock();
        try
        {
            Node pred = head;
            Node curr = pred.next;

            while (curr.value < value)
            {
                pred = curr;
                curr=curr.next;
            }

            if(curr.value == value)
            {
                return false;
            }
            else
            {
                Node node = new Node(value);
                node.next = curr;
                pred.next = node;
                return true;

            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean remove(int value) 
    {
        lock.lock();
        try
        {
            Node pred= head;
            Node curr= pred.next;
            while( curr.value < value)
            {
                pred = curr;
                curr = curr.next;
            }
            if( curr.value == value)
            {
                pred.next = curr.next;
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean contains(int value) 
    {
      lock.lock();
      try
      {
        Node curr = head;
        while (curr.value < value)
        {
            curr = curr.next;
        }
        return curr.value == value;
      }
      finally
      {
        lock.unlock();
      }
    }
}