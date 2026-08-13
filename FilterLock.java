public class FilterLock implements Lock 
{

    private final int n;
    private final VolatileInt[] level;
    private final VolatileInt[] victim;

    public FilterLock(int n) 
    {
	this.n = n;
	level = new VolatileInt[n];
	victim = new Volatile[n];

	//elke thread begin b level 0, want dit probeer nie n lock acquire nie
	for (int i = 0; i < n; i++) 
        {
            level[i] = new VolatileInt(0);
            victim[i] = new VolatileInt(0);
        }
    }

    @Override
    public void lock(int threadId) 
    {
	//thread moet deur n-1 levels kom om lock te acquire
	for (int l = 1; l <= n - 1; l++) 
        {
            level[threadId].value = l;   // announce intent to enter level l
            victim[l].value = threadId;  // offer to be the victim at level l

	    // wag as ons steeds victom is en as n ander thread by n level is 
            boolean conflictExists;

	do 
            {
                conflictExists = false;

                for (int k = 0; k < n; k++) 
                {
                    if (k != threadId && level[k].value >= l && victim[l].value == threadId) 
                    {
                        conflictExists = true;
                        break;
                    }
                }

            } while (conflictExists);
	//level n-1 bereik, kan nou critical section enter.
    }

    @Override
    public void unlock(int threadId) 
    {
    level[threadId].value = 0;      
    }
}