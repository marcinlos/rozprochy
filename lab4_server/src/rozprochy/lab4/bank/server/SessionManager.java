package rozprochy.lab4.bank.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Users.InvalidSession;
import Users.MultiLogin;
import Users.SessionException;
import Users.SessionExpired;

public class SessionManager {

    private int evictionInterval;
    private int sessionTimeout;

    // sid -> session
    private Map<String, Session> sessions = new HashMap<String, Session>();
    // pesel -> session
    private Map<String, Session> logged = new HashMap<String, Session>();
    
    private Object lock = new Object();
    
    private Map<String, String> config;
    private Thread sessionEvictor;
    private List<SessionListener> listeners = new ArrayList<SessionListener>();
    
    public SessionManager(Map<String, String> config) {
        System.out.println("Initiating session manager");
        this.config = config;
        loadProperties();
        initEvictorDeamon();
        System.out.println("Session manager activated");
    }
    
    private void loadProperties() {
        System.out.println("Loading session manager configuration");
        evictionInterval = tryParseValue("BankApp.Session.EvictPeriod", 30000);
        System.out.printf("   Eviction interval: %d ms\n", evictionInterval);
        sessionTimeout = tryParseValue("BankApp.Session.Timeout", 5000);
        System.out.printf("   Session timeout: %d ms\n", sessionTimeout);
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
    
    public void addSessionListener(SessionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public Session getSessionById(String sid) throws SessionExpired {
        synchronized (lock) {
            Session session = sessions.get(sid);
            if (session != null) {
                if (! removeIfExpired(session)) {
                    return session;
                } else {
                    throw new SessionExpired();
                }
            } else {
                return null;
            }
        }
    }
    
    public Session getSessionByUser(String pesel) throws SessionExpired {
        synchronized (lock) {
            Session session = logged.get(pesel);
            if (! removeIfExpired(session)) {
                return session;
            } else {
                throw new SessionExpired();
            }
        }
    }
    
    public void addSession(Session session) throws MultiLogin {
        String sid = session.getId();
        String pesel = session.getUser();
        synchronized (lock) {
            if (isUserLogged(pesel)) {
                throw new MultiLogin();
            }
            sessions.put(sid, session);
            logged.put(pesel, session);
            System.out.printf("New session (user=%s, sid=%s)\n", pesel, sid);
        }
    }
    
    public void keepalive(String sid) throws SessionException {
        try {
            Session session = getSessionById(sid);
            if (session != null) {
                session.touch();
            } else {
                throw new InvalidSession();
            }
            System.out.printf("Session ping (sid=%s)\n", sid);
        } catch (SessionException e) {
            System.out.printf("Session ping failed (sid=%s)\n", sid);
            throw e;
        }
    }
    
    public boolean removeSession(String sid, RemovalReason reason) 
            throws InvalidSession {
        synchronized (lock) {
            Session session = sessions.get(sid);
            if (session != null) {
                String pesel = session.getUser();
                sessions.remove(sid);
                logged.remove(pesel);
                System.out.printf("Session terminated (user=%s, sid=%s)\n" + 
                        "   reason: %s\n", pesel, sid, reason.toString());
                informAboutRemoval(sid, reason);
                return true;
            } else {
                throw new InvalidSession();
            }
        }
    }
    
    private boolean removeIfExpired(Session session) {
        if (expired(session)) {
            removeExpired(session);
            return true;
        } else {
            return false;
        }
    }
    
    private boolean expired(Session session) {
        long time = session.timeSinceUsed();
        return time > sessionTimeout;
    }
    
    private void removeExpired(Session session) {
        long time = session.timeSinceUsed();
        String sid = session.getId();
        String user = session.getUser();
        System.out.printf("Session (sid=%s, user=%s) " +
                "timed out (%.2fs)\n", sid, user, time / 1000.0);
        try {
            removeSession(sid, RemovalReason.EXPIRED);
        } catch (InvalidSession e) {
            // REALLY shouldn't happen
            System.err.println("Internal error: session removed while " +
                    "being examined by session evictor");
        }
    }
    
    private void informAboutRemoval(String sid, RemovalReason reason) {
        synchronized (listeners) {
            for (SessionListener listener: listeners) {
                listener.sessionRemoved(sid, reason);
            }
        }
    }
    
    public boolean checkSessionActive(String sid) throws SessionExpired {
        synchronized (lock) {
            return getSessionById(sid) != null;
        }
    }
    
    public boolean isUserLogged(String pesel) {
        synchronized (lock) {
            Session session = logged.get(pesel);
            if (session != null) {
                try {
                    checkSessionActive(session.getId());
                    return true;
                } catch (SessionException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
    
    private void initEvictorDeamon() {
        System.out.print("Initiating session evictor deamon...");
        System.out.flush();
        sessionEvictor = new Thread(new SessionEvictor());
        sessionEvictor.setDaemon(true);
        sessionEvictor.start();
        System.out.println("done");
    }
    
    private class SessionEvictor implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(evictionInterval);
                    runCheckPass();
                }
            } catch (InterruptedException e) {
                System.out.println("Evictor thread interrupted");
            }
        }
        
        private void runCheckPass() {
            System.out.println("Session evictor pass begins...");
            int examined = 0;
            int evicted = 0;
            synchronized (lock) {
                Set<Session> sess = new HashSet<Session>(sessions.values());
                for (Session session: sess) {
                    ++ examined;
                    if (expired(session)) {
                        ++ evicted;
                        removeExpired(session);
                    }
                }
            }
            System.out.println("Session evictor pass completed:");
            System.out.printf("   %4d examined\n", examined);
            System.out.printf("   %4d evicted\n", evicted);
        }
        
    }

}
