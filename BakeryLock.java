public class BakeryLock implements Lock {

    private final int n;
    private final VolatileBoolean[] flag;
    private final VolatileInt[] label;

    public BakeryLock(int n) {

        this.n = n;
        flag = new VolatileBoolean[n];
        label = new VolatileInt[n];

        for(int i = 0; i < n; i++)
            {
                flag[i] = new VolatileBoolean(false);
                label[i] = new VolatileInt(0);
            }
    }

    @Override
    public void lock(int threadId) {
        // thread is picking a number
        flag[threadId].value = true;

        int max = 0;
        for (int k = 0; k < n; k++) 
        {
            if (label[k].value > max) 
            {
                max = label[k].value;
            }
        }

        //setting a new max limit for the tickets 
        label[threadId].value = max + 1;

        //thread is not wating on picking a number anymore 
        flag[threadId].value = false;


        //wait for threads with earlier tickets 
        for(int k = 0; k < n; k++)
        {
            if( k == threadId) 
            {
                continue;
            }

            //wait while thread k is still in its ticket
            while (flag[k].value)
            {
                //loop checked condition
                //thread sits here till till flag k is set to false 
            }

            while(label[k].value != 0 &&
                (label[k].value < label[threadId].value ||
                (label[k].value == label[threadId].value &&
                (k < threadId))))
            {
                //thread sits here till k is realeased and becomes unlocked 
            }
        }
    }

    @Override
    public void unlock(int threadId) {
        label[threadId].value = 0;
    }
}
