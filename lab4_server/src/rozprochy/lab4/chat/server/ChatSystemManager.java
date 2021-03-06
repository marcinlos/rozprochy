package rozprochy.lab4.chat.server;

import java.util.Map;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.util.Crypto;
import Chat.CannotCreateRoom;
import Chat.MemberPrx;
import Chat.MemberPrxHelper;
import Chat.NeedForRecovery;
import Chat._SystemManagerDisp;
import Ice.Current;
import Ice.Identity;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import Users.AuthenticationFailed;
import Users.DbError;
import Users.LoginException;
import Users.RegisterException;
import Users.SessionException;

public class ChatSystemManager extends _SystemManagerDisp {

    private AccountManager accounts;
    private BiSessionManager sessions;
    private RoomManager rooms;
    
    //private ObjectAdapter adapter;
    private Map<String, String> config;
    
    private static final String PREFIX = "[Chat] ";

    public ChatSystemManager(ObjectAdapter adapter, 
            Map<String, String> config) {
        this.config = config;
        //this.adapter = adapter;
        accounts = new AccountManager("Chat", this.config);
        sessions = new BiSessionManager("ChatApp", this.config);
        
        rooms = new RoomManager(config, sessions);
        adapter.addServantLocator(rooms, "Room");
    }

    @Override
    public synchronized void createAccount(String login, String password,
            Current __current) throws RegisterException, DbError {
        System.out.printf("%sAccount creation attempt (user=%s, pwd=%s)\n", 
                PREFIX, login, password);
        accounts.create(login, password, null);
    }

    @Override
    public synchronized String login(String login, String password,
            Current __current) throws LoginException, DbError {
        System.out.printf(PREFIX + "Login attempt (user=%s, pwd=%s)\n", 
                login, password);
        if (accounts.authenticate(login, password)) {
            String sid = Crypto.createSessionId();
            BiSession session = new BiSession(sid, login);
            sessions.addSession(session);
            System.out.printf("%sLogged in (user=%s, pwd=%s)\n", PREFIX, login, 
                    password);
            return sid;
        } else {
            System.out.printf("%sAuthentication failed (user=%s, pwd=%s)\n", 
                    PREFIX, login, password);
            throw new AuthenticationFailed();
        }
    }

    @Override
    public void logout(String sessionId, Current __current)
            throws SessionException {
        System.out.printf("%sLogout (sid=%s)\n", PREFIX, sessionId);
        if (sessions.checkSessionActive(sessionId)) {
            sessions.removeSession(sessionId, RemovalReason.LOGGED_OUT);
        }
    }

    @Override
    public void keepalive(String sessionId, Current __current)
            throws SessionException, NeedForRecovery {
        try {
            sessions.keepalive(sessionId);
        } catch (InvalidCallbackException e) {
            throw new NeedForRecovery();
        }
    }

    @Override
    public String[] getRooms(String sessionId, Current __current)
            throws SessionException {
        return rooms.getRoomList().toArray(new String[0]);
    }

    @Override
    public void setCallback(String sessionId, MemberPrx callback,
            Current __current) throws SessionException {
        // Create callback proxy
        Identity id = callback.ice_getIdentity();
        ObjectPrx obj = __current.con.createProxy(id);
        MemberPrx cb = MemberPrxHelper.uncheckedCast(obj);
        sessions.addCallback(sessionId, cb);
        System.out.printf("%sEstablished bidir connection with timeout=%d ms\n",
                PREFIX, obj.ice_getConnection().timeout());
        System.out.println("Connection=" + obj.ice_getConnection());
        cb.greet("Welcomeeeee");
    }

    @Override
    public void createRoom(String name, Current __current)
            throws CannotCreateRoom {
        rooms.createRoom(name);
    }

}
