package rozprochy.lab4.chat.server;

import java.util.ArrayList;
import java.util.List;

import rozprochy.lab4.generic.Session;
import Chat.MemberPrx;

public class BiSession extends Session {
    
    private MemberPrx callback;
    private List<SessionRecoveryListener> damaged = 
            new ArrayList<SessionRecoveryListener>();
    
    private volatile long callbackTimestamp = System.currentTimeMillis();
    private boolean validCallback = false;
    private static final int ASSUMED_VALID = 50;


    public BiSession(String id, String user) {
        super(id, user);
    }
    
    public void setCallback(MemberPrx callback) {
        synchronized (damaged) {
            this.callback = callback;
            callbackTimestamp = System.currentTimeMillis();
            validCallback = true;
            for (SessionRecoveryListener listener: damaged) {
                listener.sessionRecovered(this);
            }
            damaged.clear();
        }
    }
    
    public void addRecoveryListener(SessionRecoveryListener listener) {
        synchronized (damaged) {
            long now = System.currentTimeMillis(); 
            if (now - callbackTimestamp < ASSUMED_VALID) {
                listener.sessionRecovered(this);
            } else {
                validCallback = false;
                damaged.add(listener);
            }
        }
    }
    
    public MemberPrx getCallback() {
        return callback;
    }
    
    @Override
    public void touch() {
        super.touch();
        if (! validCallback) {
            throw new InvalidCallbackException();
        }
    }

}
