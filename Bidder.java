public class Bidder implements Runnable {
    private final Auction auction;
    private final Lock lock;
    private final int bidderId;
    private final int iterations;
    private final double bidIncrement;

    private int winCount = 0;
    private long totalWaitNanos = 0; //added

    public Bidder (Auction auction, Lock lock, int bidderId, int iterations, double bidIncrement)
    {
        this.auction = auction;
        this.lock = lock;
        this.bidderId =bidderId;
        this.iterations =iterations;
        this.bidIncrement = bidIncrement;
    }
    
    @Override 
    public void run()
    {
        for (int i= 0; i< iterations; i++)
        {
            long waitStart = System.nanoTime(); //added
            lock.lock();
            totalWaitNanos += (System.nanoTime() - waitStart);  //added
            try
            {
                double currentHighest = auction.getHighestBid();
                double newBid = currentHighest + bidIncrement;
                
                auction.placeBid(bidderId, newBid);
                winCount++;
            }
            finally
            {
                lock.unlock();
            }
        }
    }


    public int getBidderId()
    {
        return bidderId;
    }


    public int getWinCount()
    {
        return winCount;
    }

    public long getTotalWaitNanos() //added
    {
        return totalWaitNanos;
    }
}
