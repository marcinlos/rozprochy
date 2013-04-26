package rozprochy.lab4.bank.server;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private Map<String, Session> sessions = new HashMap<String, Session>();
    
    private Object lock = new Object();
    
    public Session getSession(String id) {
        synchronized (lock) {
            return sessions.get(id);
        }
    }
    
    public boolean sessionActive(String id) {
        synchronized (lock) {
            return sessions.containsKey(id);
        }
    }
    
    public SessionManager() {
        // TODO Auto-generated constructor stub
    }

}
