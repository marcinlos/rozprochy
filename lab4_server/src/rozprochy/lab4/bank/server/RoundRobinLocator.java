package rozprochy.lab4.bank.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Bank.InvalidSession;
import Bank._AccountDisp;
import Ice.Current;
import Ice.Identity;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.ServantLocator;
import Ice.UserException;

public class RoundRobinLocator implements ServantLocator {

    private List<_AccountDisp> servants = new ArrayList<_AccountDisp>();
    
    private SessionManager sessions;
    private AccountManager accounts;
    
    private Lock servantListLock = new ReentrantLock();
    private int nextIndex = 0;
    
    private LoadStats stats = new LoadStats();
    
    private Map<String, String> config;
    
    private boolean logCalls = false;
    
    /** How many calls per second is one servant supposedly ready to handler */
    private int maxPerServant = 10;
    
    public RoundRobinLocator(final SessionManager sessions, 
            final AccountManager accounts, Map<String, String> config) {
        this.sessions = sessions;
        this.accounts = accounts;
        this.config = config;
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
    
    private AccountImpl makeServant(SessionManager sessions,
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
        System.out.println("Loading locator configuration");
        String val = config.get("BankApp.Locator.Balanced.LogCalls");
        if (val != null) {
            logCalls = Boolean.valueOf(val);
        }
        System.out.println("   log calls? " + (logCalls ? "yes" : "no"));
        maxPerServant = tryParseValue(
                "BankApp.Locator.Balanced.MaxCPSPerServant", 10);
        System.out.println("   max calls/s per servant: " + maxPerServant);
    }
    
    private int tryParseValue(String prop, int defaultValue) {
        String val = config.get(prop);
        if (val != null) {
            try {
                return Integer.valueOf(val);
            } catch (NumberFormatException e) {
                System.out.printf("   (invalid value '%s')'n", val);
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public Object locate(Current curr, LocalObjectHolder cookie)
            throws UserException {
        if (logCalls) {
            logRequest(curr);
        }
        String session = curr.id.name;
        if (sessions.checkSessionActive(session)) {
            return getNext();
        } else {
            throw new InvalidSession();
        }
    }

    @Override
    public void finished(Current curr, Object servant, java.lang.Object cookie)
            throws UserException {
        if (logCalls) {
            System.out.println("After request " + curr.requestId);
        }
    }

    @Override
    public void deactivate(String category) {
        System.out.println("Locator deactivated");
    }
    
    private synchronized _AccountDisp getNext() {
        servantListLock.lock();
        try {
            stats.called();
            nextIndex = (nextIndex + 1) % servants.size();
            return servants.get(nextIndex);
        } finally {
            servantListLock.unlock();
        }
    }
    
    private String idToString(Identity id) {
        return id.category + "/" + id.name;
    }
    
    private void logRequest(Current curr) {
        String adapter = curr.adapter.getName();
        int reqId = curr.requestId;
        String operation = curr.operation;
        String con = curr.con._toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Adapter: ").append(adapter).append("\n")
          .append("Request ID: ").append(reqId).append("\n")
          .append("Target: ").append(idToString(curr.id)).append("\n")
          .append("Operation: ").append(operation).append("\n")
          .append("Connection:\n").append(con).append("\n");
        System.out.println(sb.toString());
    }

}
