package rozprochy.lab4.bank.server;

import java.util.Map;

import rozprochy.lab4.generic.Session;
import rozprochy.lab4.generic.SessionManager;
import Bank._SystemManagerDisp;
import Ice.Current;
import Ice.ObjectAdapter;
import Ice.ServantLocator;
import Users.DbError;
import Users.RegisterException;
import Users.SessionException;

public class SystemManagerImpl extends _SystemManagerDisp {

    private AccountManager accounts;
    private SessionManager<Session> sessions;

    private ObjectAdapter adapter;
    private Map<String, String> config;
    
    private static final String PREFIX = "[Bank] ";

    public SystemManagerImpl(ObjectAdapter adapter, 
            Map<String, String> config) {
        this.config = config;
        this.adapter = adapter;
        accounts = new AccountManager("Bank", this.config);
        sessions = new SessionManager<Session>("BankApp", this.config);
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
    public void createAccount(String pesel, String password,
            Current __current) throws RegisterException, DbError {
        System.out.printf(PREFIX + "Account creation attempt (user=%s, pwd=%s)\n", 
                pesel, password);
        synchronized (accounts) {
            accounts.create(pesel, password, null);
        }
    }

    @Override
    public void keepalive(String sessionId, Current __current)
            throws SessionException {
        sessions.keepalive(sessionId);
    }

}
