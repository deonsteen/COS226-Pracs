public interface Lock
{
    void lock();
    void unlock();
    long getTestAndSetCount();
}
