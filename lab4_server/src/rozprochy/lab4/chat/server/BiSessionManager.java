package rozprochy.lab4.chat.server;

import java.util.Map;

import Users.InvalidSession;
import Users.SessionException;
import Users.SessionExpired;

import Chat.MemberPrx;
import Chat.NeedForRecovery;

import rozprochy.lab4.generic.SessionManager;

public class BiSessionManager extends SessionManager<BiSession> {
    
    private static final String PREFIX = "[Chat] ";
    
    public BiSessionManager(String prefix, Map<String, String> config) {
        super(prefix, config);
    }
    
    void addCallback(String sid, MemberPrx callback) throws SessionExpired {
        synchronized (lock) {
            BiSession session = sessions.get(sid);
            if (session != null) {
                if (! removeIfExpired(session)) {
                    session.setCallback(callback);
                    System.out.printf("%sCallback added (sid=%s)\n", 
                            PREFIX, sid);
                } else {
                    throw new SessionExpired();
                }
            }
        }
    }
    
    public void keepalive(String sid) throws SessionException {
        try {
            BiSession session = getSessionById(sid);
            if (session != null) {
                session.touch();
            } else {
                throw new InvalidSession();
            }
            System.out.printf(PREFIX + "Session ping (sid=%s)\n", sid);
        } catch (SessionException e) {
            System.out.printf(PREFIX + "Session ping failed (sid=%s)\n", sid);
            throw e;
        }
    }

}
