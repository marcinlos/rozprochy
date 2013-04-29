package rozprochy.lab4.chat.server;

import java.util.Map;

import Users.SessionExpired;

import Chat.MemberPrx;

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

}
