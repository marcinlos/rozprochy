package rozprochy.lab4.bank.server;

import java.util.Map;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.generic.Session;
import rozprochy.lab4.generic.SessionManager;
import rozprochy.lab4.util.Crypto;
import Bank._SystemManagerDisp;
import Ice.Current;
import Ice.ObjectAdapter;
import Ice.ServantLocator;
import Users.AuthenticationFailed;
import Users.DbError;
import Users.LoginException;
import Users.RegisterException;
import Users.SessionException;

public class SystemManagerImpl extends _SystemManagerDisp {

    private AccountManager accounts;
    private SessionManager sessions;

    private ObjectAdapter adapter;
    private Map<String, String> config;
    
    private static final String PREFIX = "[Bank] ";

    public SystemManagerImpl(ObjectAdapter adapter, 
            Map<String, String> config) {
        this.config = config;
        this.adapter = adapter;
        accounts = new AccountManager("Bank", this.config);
        sessions = new SessionManager("BankApp", this.config);
        String locatorType = config.get("BankApp.Locator");
        if (locatorType == null) {
            System.out.println(PREFIX + "Locator type unspecified, " +
                    "using default");
        }
        try {
            ServantLocator locator = LocatorFactory.newInstance(locatorType, 
                    sessions, accounts, config);
            this.adapter.addServantLocator(locator, "");
            System.out.println(PREFIX + "Created locator '" + locatorType + "'");
        } catch (UnknownLocatorType e) {
            System.err.println(PREFIX + "Unknown locator type: '" + 
                    locatorType + "'");
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void createAccount(String pesel, String password,
            Current __current) throws RegisterException, DbError {
        System.out.printf(PREFIX + "Account creation attempt (user=%s, pwd=%s)\n", 
                pesel, password);
        accounts.create(pesel, password, null);
    }

    @Override
    public synchronized String login(String pesel, String password,
            Current __current) throws LoginException, DbError {
        System.out.printf("PREFIX + Login attempt (user=%s, pwd=%s)\n", 
                pesel, password);
        if (accounts.authenticate(pesel, password)) {
            String sid = Crypto.createSessionId();
            Session session = new Session(sid, pesel);
            sessions.addSession(session);
            System.out.printf(PREFIX + "Logged in (user=%s, pwd=%s)\n", pesel, 
                    password);
            return sid;
        } else {
            System.out.printf(PREFIX + "Authentication failed (user=%s, "  +
                    "pwd=%s)\n", pesel, password);
            throw new AuthenticationFailed();
        }
    }

    @Override
    public void logout(String sessionId, Current __current)
            throws SessionException {
        System.out.printf(PREFIX + "Logout (sid=%s)\n", sessionId);
        if (sessions.checkSessionActive(sessionId)) {
            sessions.removeSession(sessionId, RemovalReason.LOGGED_OUT);
        }
    }

    @Override
    public void keepalive(String sessionId, Current __current)
            throws SessionException {
        sessions.keepalive(sessionId);
    }

}
