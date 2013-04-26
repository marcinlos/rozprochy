package rozprochy.lab4.bank.server;

import java.util.HashMap;
import java.util.Map;

import Bank.LoginException;
import Bank.RegisterException;
import Bank.SessionException;
import Bank._SystemManagerDisp;
import Ice.Current;
import Ice.ObjectAdapter;

public class SystemManagerImpl extends _SystemManagerDisp {
    
    private Map<String, Account> accounts = new HashMap<String, Account>();
    private SessionManager sessions = new SessionManager();
    
    private ObjectAdapter adapter;
    
    public SystemManagerImpl(ObjectAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void createAccount(String pesel, String password,
            Current __current) throws RegisterException {
        throw new RegisterException();
    }

    @Override
    public String login(String pesel, String password, Current __current)
            throws LoginException {
        throw new LoginException();
    }

    @Override
    public String logout(String sessionId, Current __current)
            throws SessionException {
        throw new SessionException();
    }

}
