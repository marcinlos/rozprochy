package rozprochy.lab4.bank.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.generic.Session;
import rozprochy.lab4.generic.SessionListener;
import rozprochy.lab4.generic.SessionManager;
import Bank._AccountDisp;
import Users.InvalidSession;
import Users.SessionException;

public class PerSessionLocator extends AbstractLocator {
    
    private Lock lock = new ReentrantLock();
    private Map<String, _AccountDisp> servantMap = 
            new HashMap<String, _AccountDisp>();

    public PerSessionLocator(SessionManager<Session> sessions, 
            AccountManager accounts, Map<String, String> config) {
        super(sessions, accounts, config);
        sessions.addSessionListener(new SessionListener<Session>() {
            @Override
            public void sessionRemoved(Session session, RemovalReason reason) {
                lock.lock();
                try {
                    String sid = session.getId();
                    servantMap.remove(sid);
                    System.out.printf("Servant removed (sid=%s)\n", sid);
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    @Override
    protected _AccountDisp getServant(String sid) throws SessionException {
        lock.lock();
        try {
            _AccountDisp acc = servantMap.get(sid);
            if (acc == null) {
                Session session = sessions.getSessionById(sid);
                if (session != null) {
                    String user = session.getUser();
                    acc = new AccountImpl(sessions, accounts);
                    servantMap.put(sid, acc);
                    System.out.printf("Servant created (user=%s, sid=%s)\n",
                            user, sid);
                } else {
                    System.out.println("Sdfsdfdfd");
                    throw new InvalidSession();
                }
            }
            return acc;
        } finally {
            lock.unlock();
        }
    }

}
