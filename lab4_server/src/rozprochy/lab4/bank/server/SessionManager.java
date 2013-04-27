package rozprochy.lab4.bank.server;

import java.util.HashMap;
import java.util.Map;

import Bank.MultiLogin;

public class SessionManager {

    // sid -> session
    private Map<String, Session> sessions = new HashMap<String, Session>();
    // pesel -> session
    private Map<String, Session> logged = new HashMap<String, Session>();
    
    private Object lock = new Object();
    
    
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

}
