package rozprochy.lab4.chat.server;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.generic.Session;
import Chat.MemberPrx;
import Chat.Message;
import Chat.NotAMember;
import Chat._RoomDisp;
import Ice.Current;
import Users.SessionException;

public class RoomImpl extends _RoomDisp {
    
    private RoomManager manager;
    private BiSessionManager sessions;
    private String name;
    
    private final String prefix;

    public RoomImpl(String name, RoomManager manager, BiSessionManager sessions) {
        this.name = name;
        this.manager = manager;
        this.prefix = "[Chat, " + name + "]";
        System.out.println(prefix + "Room '" + name + "' created");
    }
    
    public String getName() {
        return name;
    }
    
    public void userSessionTerminated(String user, RemovalReason reason) {
        System.out.println(prefix + "User " + user + " is gone (" + reason + ")");
    }

    @Override
    public void join(String sessionId, MemberPrx callback, Current __current)
            throws SessionException {
        Session session = sessions.getSessionById(sessionId);
        String user = session.getUser();
        System.out.println(prefix + "User " + user + " joined");
        manager.addUserRoom(user, name);
        // TODO Inform everyone
    }

    @Override
    public void leave(String sessionId, Current __current) throws NotAMember,
            SessionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendMessage(String sessionId, String text, Current __current)
            throws NotAMember, SessionException {
        // TODO Auto-generated method stub

    }

    @Override
    public Message[] fetchMessages(long since, Current __current)
            throws NotAMember, SessionException {
        // TODO Auto-generated method stub
        return null;
    }

}
