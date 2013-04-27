package rozprochy.lab4.bank.server;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoadStats {
    
    /** How many seconds should be used for calculating average value */
    private static final int SMOOTHNESS = 5;

    private Lock lock = new ReentrantLock();
    private Deque<Long> seconds = new ArrayDeque<Long>();
    
    public LoadStats() {
        seconds.offerLast(0L);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                nextSecond();
            }
        }, 1000, 1000);
    }
    
    public void called() {
        lock.lock();
        try {
            Long val = seconds.pollLast();
            seconds.offer(val + 1);
        } finally {
            lock.unlock();
        }
    }
    
    public double callsPerSecond() {
        lock.lock();
        try {
            long total = 0;
            for (long val: seconds) {
                total += val;
            }
            return total / (double) seconds.size();
        } finally {
            lock.unlock();
        }
    }
    
    private void nextSecond() {
        lock.lock();
        try {
            if (seconds.size() >= SMOOTHNESS) {
                seconds.pollFirst();
            }
            seconds.offerLast(0L);
        } finally {
            lock.unlock();
        }
    }

}
