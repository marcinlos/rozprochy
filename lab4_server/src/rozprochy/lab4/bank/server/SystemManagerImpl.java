package rozprochy.lab4.bank.server;

import Bank.LoginException;
import Bank.RegisterException;
import Bank.SessionException;
import Bank._SystemManagerDisp;
import Ice.Current;

public class SystemManagerImpl extends _SystemManagerDisp {

    public SystemManagerImpl() {
        // TODO Auto-generated constructor stub
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
