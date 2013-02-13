package rozprochy.rok2011.lab2.zad1.client.rmi;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import rozprochy.rok2011.lab2.zad1.common.Task;

/**
 * Task that executes for a possibly long time, to test time measurement
 */
public class LongTask implements Task<String>, Serializable {

    private long amount;
    private TimeUnit unit;
    
    public LongTask(long delay, TimeUnit unit) {
        this.amount = delay;
        this.unit = unit;
    }
    
    @Override
    public String execute() {
        try {
            unit.sleep(amount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Thread was interrupted";
        }
        return "Some really important text";        
    }
    
    @Override
    public String toString() {
        return "Sleep for " + unit.toMillis(amount) + "ms";
    }

}
