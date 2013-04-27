package rozprochy.lab4.bank.server;

import rozprochy.lab4.bank.util.Crypto;
import Bank.AuthenticationFailed;
import Bank.LoginException;
import Bank.RegisterException;
import Bank.SessionException;
import Bank._SystemManagerDisp;
import Ice.Current;
import Ice.ObjectAdapter;
import Ice.ServantLocator;

public class SystemManagerImpl extends _SystemManagerDisp {

    private AccountManager accounts = new AccountManager();
    private SessionManager sessions = new SessionManager();

    private ObjectAdapter adapter;

    public SystemManagerImpl(ObjectAdapter adapter) {
        this.adapter = adapter;
        ServantLocator locator = new RoundRobinLocator(sessions);
        adapter.addServantLocator(locator, "");
    }

    @Override
    public synchronized void createAccount(String pesel, String password,
            Current __current) throws RegisterException {
        accounts.create(pesel, password);
    }

    @Override
    public synchronized String login(String pesel, String password,
            Current __current) throws LoginException {
        if (accounts.authenticate(pesel, password)) {
            String sid = Crypto.createSessionId();
            Session session = new Session(sid, pesel);
            sessions.addSession(session);
            return sid;
        } else {
            throw new AuthenticationFailed();
        }
    }

    @Override
    public String logout(String sessionId, Current __current)
            throws SessionException {
        throw new SessionException();
    }

}
