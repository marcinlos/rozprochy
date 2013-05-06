package rozprochy.lab4.bank.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rozprochy.lab4.generic.Session;
import rozprochy.lab4.generic.SessionManager;
import Bank._AccountDisp;


public class RoundRobinLocator extends AbstractLocator {

    private List<_AccountDisp> servants = new ArrayList<_AccountDisp>();
    
    private Lock servantListLock = new ReentrantLock();
    private int nextIndex = 0;
    
    private LoadStats stats = new LoadStats();
    
    /** How many calls per second is one servant supposedly ready to handler */
    private int maxPerServant = 10;
    
    public RoundRobinLocator(final SessionManager<Session> sessions, 
            final AccountManager accounts, Map<String, String> config) {
        super(sessions, accounts, config);
        loadConfig();
        AccountImpl acc = makeServant(sessions, accounts);
        servants.add(acc);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int needed = (int)(stats.callsPerSecond() / maxPerServant);
                if (needed == 0) {
                    needed = 1;
                }
                int diff = setServantCount(needed);
                if (diff > 0) {
                    System.out.printf("%d servants removed (%d total)\n",
                            diff, needed);
                } else if (diff < 0) {
                    System.out.printf("%d servants added (%d total)\n",
                            -diff, needed);
                }
                // System.out.println("Calls/s: " + stats.callsPerSecond());
            }
        }, 1000, 1000);
        System.out.println("Servant locator created");
    }
    
    private AccountImpl makeServant(SessionManager<Session> sessions,
            AccountManager accounts) {
        return new AccountImpl(sessions, accounts);
    }
    
    private int setServantCount(int count) {
        int diff = servants.size() - count;
        if (diff != 0) {
            servantListLock.lock();
            try {
                if (diff > 0) {
                    for (int i = 0; i < diff; ++ i) {
                        servants.remove(servants.size() - 1);
                    }
                } else {
                    for (int i = 0; i < -diff; ++ i) {
                        servants.add(makeServant(sessions, accounts));
                    }
                }
            } finally {
                servantListLock.unlock();
            }
        }
        return diff;
    }
    
    private void loadConfig() {
        maxPerServant = tryParseValue(
                "BankApp.Locator.Balanced.MaxCPSPerServant", 10);
        System.out.println("   max calls/s per servant: " + maxPerServant);
    }
    
    protected _AccountDisp getServant(String sid) {
        servantListLock.lock();
        try {
            stats.called();
            nextIndex = (nextIndex + 1) % servants.size();
            return servants.get(nextIndex);
        } finally {
            servantListLock.unlock();
        }
    }
    
}
