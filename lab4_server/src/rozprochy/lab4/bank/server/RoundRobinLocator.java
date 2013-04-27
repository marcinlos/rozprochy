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

    private SessionManager sessions;
    private List<_AccountDisp> servants = new ArrayList<_AccountDisp>();
    
    private Lock lock = new ReentrantLock();
    private int nextIndex = 0;
    
    private LoadStats stats = new LoadStats();
    
    private Map<String, String> config;
    private boolean logCalls = false;
    
    public RoundRobinLocator(SessionManager sessions, 
            Map<String, String> config) {
        this.sessions = sessions;
        this.config = config;
        loadConfig();
        AccountImpl acc = new AccountImpl();
        servants.add(acc);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Calls/s: " + stats.callsPerSecond());
            }
        }, 1000, 1000);
        System.out.println("Servant locator created");
    }
    
    private void loadConfig() {
        String val = config.get("BankApp.Locator.Balanced.LogCalls");
        if (val != null) {
            logCalls = Boolean.valueOf(val);
        }
        System.out.println("Locator: log calls? " + (logCalls ? "yes" : "no"));
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

    }
    
    private synchronized _AccountDisp getNext() {
        lock.lock();
        try {
            stats.called();
            int idx = nextIndex;
            nextIndex = (nextIndex + 1) % servants.size();
            return servants.get(idx);
        } finally {
            lock.unlock();
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
