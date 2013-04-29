package rozprochy.lab4.chat.server;

import java.util.Map;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.generic.Session;
import rozprochy.lab4.generic.SessionManager;
import rozprochy.lab4.util.Crypto;
import Chat.MemberPrx;
import Chat.MemberPrxHelper;
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
    private SessionManager sessions;

    private ObjectAdapter adapter;
    private Map<String, String> config;
    
    private static final String PREFIX = "[Chat] ";

    public ChatSystemManager(ObjectAdapter adapter, 
            Map<String, String> config) {
        this.config = config;
        this.adapter = adapter;
        accounts = new AccountManager("Chat", this.config);
        sessions = new SessionManager("ChatApp", this.config);
        /*String locatorType = config.get("BankApp.Locator");
        if (locatorType == null) {
            System.out.println("Locator type unspecified, using default");
        }
        try {
            ServantLocator locator = LocatorFactory.newInstance(locatorType, 
                    sessions, accounts, config);
            this.adapter.addServantLocator(locator, "");
            System.out.println("Created locator '" + locatorType + "'");
        } catch (UnknownLocatorType e) {
            System.err.println("Unknown locator type: '" + locatorType + "'");
            throw new RuntimeException(e);
        }*/
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
        System.out.printf("PREFIX + Login attempt (user=%s, pwd=%s)\n", 
                login, password);
        if (accounts.authenticate(login, password)) {
            String sid = Crypto.createSessionId();
            Session session = new Session(sid, login);
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
            throws SessionException {
        sessions.keepalive(sessionId);
    }

    @Override
    public String[] getRooms(String sessionId, Current __current)
            throws SessionException {
        return new String[] { "Aleph", "Beth", "Gimmel" };
    }

    @Override
    public void setCallback(String sessionId, MemberPrx callback,
            Current __current) throws SessionException {
        // Create callback proxy
        Identity id = callback.ice_getIdentity();
        ObjectPrx obj = __current.con.createProxy(id);
        MemberPrx cb = MemberPrxHelper.uncheckedCast(obj);
        cb.greet("Welcomeeeee");
    }

}
