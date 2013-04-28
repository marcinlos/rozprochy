package rozprochy.lab4.bank.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Bank._AccountDisp;
import Ice.Current;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.ServantLocator;
import Ice.UserException;

public class PerSessionLocator implements ServantLocator {
    
    private SessionManager sessions;
    private AccountManager accounts;
    
    private Map<String, String> config;
    
    private Lock lock = new ReentrantLock();
    private Map<String, _AccountDisp> servantMap = 
            new HashMap<String, _AccountDisp>();

    public PerSessionLocator(SessionManager sessions, AccountManager accounts, 
            Map<String, String> config) {
        this.sessions = sessions;
        this.accounts = accounts;
        this.config = config;
        sessions.addSessionListener(new SessionListener() {
            @Override
            public void sessionRemoved(String sid, RemovalReason reason) {
                lock.lock();
                try {
                    servantMap.remove(sid);
                    System.out.printf("Servant removed (sid=%s)\n", sid);
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    @Override
    public Object locate(Current curr, LocalObjectHolder cookie)
            throws UserException {
        lock.lock();
        try {
            String sid = curr.id.name;
            _AccountDisp acc = servantMap.get(sid);
            if (acc == null) {
                Session session = sessions.getSessionById(sid);
                String user = session.getUser();
                acc = new AccountImpl(sessions, accounts);
                servantMap.put(sid, acc);
                System.out.printf("Servant created (user=%s, sid=%s)\n",
                        user, sid);
            }
            return acc;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void finished(Current curr, Object servant, java.lang.Object cookie)
            throws UserException {
        // empty
    }

    @Override
    public void deactivate(String category) {
        System.out.println("\nLocator deactivated");
    }

}
