package rozprochy.lab4.bank.server;

import java.util.Map;

import rozprochy.lab4.util.Crypto;
import Bank.AuthenticationFailed;
import Bank.LoginException;
import Bank.RegisterException;
import Bank.SessionException;
import Bank._SystemManagerDisp;
import Ice.Current;
import Ice.ObjectAdapter;
import Ice.ServantLocator;

public class SystemManagerImpl extends _SystemManagerDisp {

    private AccountManager accounts;
    private SessionManager sessions;

    private ObjectAdapter adapter;
    private Map<String, String> config;

    public SystemManagerImpl(ObjectAdapter adapter, 
            Map<String, String> config) {
        this.config = config;
        this.adapter = adapter;
        accounts = new AccountManager(this.config);
        sessions = new SessionManager(this.config);
        ServantLocator locator = new RoundRobinLocator(sessions, 
                accounts, this.config);
        this.adapter.addServantLocator(locator, "");
    }

    @Override
    public synchronized void createAccount(String pesel, String password,
            Current __current) throws RegisterException {
        System.out.printf("Account creation attempt (user=%s, pwd=%s)\n", 
                pesel, password);
        accounts.create(pesel, password);
    }

    @Override
    public synchronized String login(String pesel, String password,
            Current __current) throws LoginException {
        System.out.printf("Login attempt (user=%s, pwd=%s)\n", pesel, password);
        if (accounts.authenticate(pesel, password)) {
            String sid = Crypto.createSessionId();
            Session session = new Session(sid, pesel);
            sessions.addSession(session);
            System.out.printf("Logged in (user=%s, pwd=%s)\n", pesel, password);
            return sid;
        } else {
            System.out.printf("Authentication failed (user=%s, pwd=%s)\n", 
                    pesel, password);
            throw new AuthenticationFailed();
        }
    }

    @Override
    public void logout(String sessionId, Current __current)
            throws SessionException {
        System.out.printf("Logout (sid=%s)\n", sessionId);
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
