/*Optional Helper Runner Class*/
public class Runner
{

    public final int numberOfThreads;
    public final int iterations;
    public final Auction auction;
    public final Lock lock;

    private Bidder[] bidders;
    private long executionTimeNanos;

    public Runner(int numberOfThreads,int iterations,Auction auction,Lock lock)
    {
        this.numberOfThreads = numberOfThreads;
        this.iterations = iterations;
        this.auction = auction;
        this.lock = lock;
    }

    public void run() throws InterruptedException
    {
        Thread[] threads = new Thread[numberOfThreads];
        bidders = new Bidder[numberOfThreads];

        for(int i = 0; i < numberOfThreads; i++)
        {
            bidders[i] = new Bidder(auction, lock, i, iterations, 1.0);
            threads[i] = new Thread(bidders[i]);
        }

        long startTime = System.nanoTime();

        for(Thread thread : threads)
        {
            thread.start();
        }

        for(Thread thread : threads)
        {
            thread.join();
        }

        long endTime = System.nanoTime();
        executionTimeNanos = endTime - startTime;

        reportResults(executionTimeNanos);
    }

    /*Optional Helper: Records and reports the results of the experiment.*/
    public void reportResults(long executionTime)
    {
        System.out.printf("Execution time: %.3f ms%n", executionTime / 1_000_000.0);
        System.out.println("Total bids placed: " + getTotalBids());
        System.out.printf("Final highest bid: %.2f (bidder %d)%n", auction.getHighestBid(), auction.getHighestBidder());
        System.out.println("Bids won per bidder:");
        for (Bidder b : bidders)
        {
            System.out.println("  Bidder " + b.getBidderId() + ": " + b.getWinCount());
        }
    }

    /*Sum of each bidder's own successful bids. Since every bidder always bids
     *strictly above the value it read under the lock, this doubles as a
     *correctness check: it should equal numberOfThreads * iterations exactly.*/
    public int getTotalBids()
    {
        int total = 0;
        for (Bidder b : bidders)
        {
            total += b.getWinCount();
        }
        return total;
    }

    public long getExecutionTimeNanos()
    {
        return executionTimeNanos;
    }

    public Bidder[] getBidders()
    {
        return bidders;
    }
}
