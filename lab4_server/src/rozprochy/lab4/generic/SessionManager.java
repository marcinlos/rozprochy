package rozprochy.lab4.generic;

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

public class SessionManager<T extends Session> {

    private int evictionInterval;
    private int sessionTimeout;

    // sid -> session
    protected Map<String, T> sessions = new HashMap<String, T>();
    // login -> session
    protected Map<String, T> logged = new HashMap<String, T>();
    
    protected Object lock = new Object();
    
    private Map<String, String> config;
    private Thread sessionEvictor;
    private List<SessionListener> listeners = new ArrayList<SessionListener>();
    
    private String prefix;
    private String confPrefix;
    
    public SessionManager(String prefix, Map<String, String> config) {
        this.prefix = String.format("[%s] ", prefix);
        this.confPrefix = prefix + ".Session";
        System.out.println(this.prefix + "Initiating session manager");
        this.config = config;
        loadProperties();
        initEvictorDeamon();
        System.out.println(this.prefix + "Session manager activated");
    }
    
    protected void loadProperties() {
        System.out.println(prefix + "Loading session manager configuration");
        evictionInterval = tryParseValue(confPrefix + ".EvictPeriod", 30000);
        System.out.printf(prefix + "   Eviction interval: %d ms\n", 
                evictionInterval);
        sessionTimeout = tryParseValue(confPrefix + ".Timeout", 5000);
        System.out.printf(prefix + "   Session timeout: %d ms\n", 
                sessionTimeout);
    }
    
    protected int tryParseValue(String prop, int defaultValue) {
        String val = config.get(prop);
        if (val != null) {
            try {
                return Integer.valueOf(val);
            } catch (NumberFormatException e) {
                System.out.printf(prefix + "   (invalid value '%s')'n", val);
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
    
    public T getSessionById(String sid) throws SessionExpired {
        synchronized (lock) {
            T session = sessions.get(sid);
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
    
    public T getSessionByUser(String login) throws SessionExpired {
        synchronized (lock) {
            T session = logged.get(login);
            if (! removeIfExpired(session)) {
                return session;
            } else {
                throw new SessionExpired();
            }
        }
    }
    
    public void addSession(T session) throws MultiLogin {
        String sid = session.getId();
        String pesel = session.getUser();
        synchronized (lock) {
            if (isUserLogged(pesel)) {
                throw new MultiLogin();
            }
            sessions.put(sid, session);
            logged.put(pesel, session);
            System.out.printf(prefix + "New session (user=%s, sid=%s)\n", 
                    pesel, sid);
        }
    }
    
    public void keepalive(String sid) throws SessionException {
        try {
            T session = getSessionById(sid);
            if (session != null) {
                session.touch();
            } else {
                throw new InvalidSession();
            }
            System.out.printf(prefix + "Session ping (sid=%s)\n", sid);
        } catch (SessionException e) {
            System.out.printf(prefix + "Session ping failed (sid=%s)\n", sid);
            throw e;
        }
    }
    
    public boolean removeSession(String sid, RemovalReason reason) 
            throws InvalidSession {
        synchronized (lock) {
            T session = sessions.get(sid);
            if (session != null) {
                String pesel = session.getUser();
                sessions.remove(sid);
                logged.remove(pesel);
                System.out.printf(prefix + "Session terminated (user=%s, " + 
                        "sid=%s)\n   reason: %s\n", pesel, sid, 
                        reason.toString());
                informAboutRemoval(sid, reason);
                return true;
            } else {
                throw new InvalidSession();
            }
        }
    }
    
    protected boolean removeIfExpired(T session) {
        if (expired(session)) {
            removeExpired(session);
            return true;
        } else {
            return false;
        }
    }
    
    protected boolean expired(T session) {
        long time = session.timeSinceUsed();
        return time > sessionTimeout;
    }
    
    protected void removeExpired(T session) {
        long time = session.timeSinceUsed();
        String sid = session.getId();
        String user = session.getUser();
        System.out.printf(prefix + "Session (sid=%s, user=%s) " +
                "timed out (%.2fs)\n", sid, user, time / 1000.0);
        try {
            removeSession(sid, RemovalReason.EXPIRED);
        } catch (InvalidSession e) {
            // REALLY shouldn't happen
            System.err.println(prefix + "Internal error: session removed " + 
                    "while being examined by session evictor");
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
    
    public boolean isUserLogged(String login) {
        synchronized (lock) {
            T session = logged.get(login);
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
        System.out.print(prefix + "Initiating session evictor deamon...");
        System.out.flush();
        sessionEvictor = new Thread(createEvictor());
        sessionEvictor.setDaemon(true);
        sessionEvictor.start();
        System.out.println("done");
    }
    
    protected Runnable createEvictor() {
        return new SessionEvictor();
    }
    
    protected class SessionEvictor implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(evictionInterval);
                    runCheckPass();
                }
            } catch (InterruptedException e) {
                System.out.println(prefix + "Evictor thread interrupted");
            }
        }
        
        private void runCheckPass() {
            System.out.println(prefix + "Session evictor pass begins...");
            int examined = 0;
            int evicted = 0;
            synchronized (lock) {
                Set<T> sess = new HashSet<T>(sessions.values());
                for (T session: sess) {
                    ++ examined;
                    if (expired(session)) {
                        ++ evicted;
                        removeExpired(session);
                        sessionRemoved(session);
                    }
                }
            }
            System.out.println(prefix + "Session evictor pass completed:");
            System.out.printf(prefix + "   %4d examined\n", examined);
            System.out.printf(prefix + "   %4d evicted\n", evicted);
        }
        
        protected void sessionRemoved(T session) {
            // empty
        }
        
    }

}
