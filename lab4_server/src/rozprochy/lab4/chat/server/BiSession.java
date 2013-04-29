package rozprochy.lab4.chat.server;

import Chat.MemberPrx;
import rozprochy.lab4.generic.Session;

public class BiSession extends Session {
    
    private MemberPrx callback;

    public BiSession(String id, String user) {
        super(id, user);
        // TODO Auto-generated constructor stub
    }
    
    public void setCallback(MemberPrx callback) {
        this.callback = callback;
    }
    
    public MemberPrx getCallback() {
        return callback;
    }

}
