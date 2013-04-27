package rozprochy.lab4.bank.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Bank.InvalidSession;
import Bank.MultiLogin;

public class SessionManager {

    private static final int EVICT_INTERVAL = 5000;
    private static final int TIMEOUT = 10000;

    // sid -> session
    private Map<String, Session> sessions = new HashMap<String, Session>();
    // pesel -> session
    private Map<String, Session> logged = new HashMap<String, Session>();
    
    private Object lock = new Object();
    
    private Thread sessionEvictor;
    private List<SessionListener> listeners = new ArrayList<SessionListener>();
    
    public SessionManager() {
        System.out.println("Initiating session manager");
        initEvictorDeamon();
        System.out.println("Session manager activated");
    }
    
    public void addSessionListener(SessionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public Session getSessionById(String sid) {
        synchronized (lock) {
            return sessions.get(sid);
        }
    }
    
    public Session getSessionByUser(String pesel) {
        synchronized (lock) {
            return logged.get(pesel);
        }
    }
    
    public void addSession(Session session) throws MultiLogin {
        String sid = session.getId();
        String pesel = session.getUser();
        synchronized (lock) {
            if (logged.containsKey(pesel)) {
                throw new MultiLogin();
            }
            sessions.put(sid, session);
            logged.put(pesel, session);
            System.out.printf("New session (user=%s, sid=%s)\n", pesel, sid);
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
                System.out.printf("Session terminated (user=%s, sid=%s), " + 
                        "reason: %s\n", pesel, sid, reason.toString());
                informAboutRemoval(sid, reason);
                return true;
            } else {
                throw new InvalidSession();
            }
        }
    }
    
    private void informAboutRemoval(String sid, RemovalReason reason) {
        synchronized (listeners) {
            for (SessionListener listener: listeners) {
                listener.sessionRemoved(sid, reason);
            }
        }
    }
    
    public boolean isSessionActive(String sid) {
        synchronized (lock) {
            return sessions.containsKey(sid);
        }
    }
    
    public boolean isUserLogged(String pesel) {
        synchronized (lock) {
            return logged.containsKey(pesel);
        }
    }
    
    private void initEvictorDeamon() {
        System.out.print("Initiating session evictor deamon...");
        System.out.println("flush");
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
                    Thread.sleep(EVICT_INTERVAL);
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
                for (Entry<String, Session> entry: sessions.entrySet()) {
                    ++ examined;
                    Session session = entry.getValue();
                    long time = session.timeSinceUsed();
                    if (time > TIMEOUT) {
                        String sid = session.getId();
                        String user = session.getUser();
                        System.out.printf("Session (sid=%s, user=%s) " +
                                "timed out (%.2fs)\n", sid, user, time / 1000.0);
                        try {
                            removeSession(sid, RemovalReason.EXPIRED);
                            ++ evicted;
                        } catch (InvalidSession e) {
                            // REALLY shouldn't happen
                            System.err.println("Internal error: session " +
                                    " removed while being examined by " + 
                                    "session evictor");
                        }
                    }
                }
            }
            System.out.println("Session evictor pass completed:");
            System.out.printf("  %4d examined\n", examined);
            System.out.printf("  %4d evicted\n", evicted);
        }
        
    }

}
