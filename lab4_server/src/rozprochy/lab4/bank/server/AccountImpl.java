package rozprochy.lab4.bank.server;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.generic.Session;
import rozprochy.lab4.generic.SessionManager;
import Bank.OperationException;
import Bank._AccountDisp;
import Ice.Current;
import Users.DbError;
import Users.LoginException;
import Users.SessionException;
import Users.SessionExpired;

public class AccountImpl extends _AccountDisp {

    protected SessionManager<Session> sessions;
    protected AccountManager accounts;
    
    public AccountImpl(SessionManager<Session> sessions, 
            AccountManager accounts) {
        this.sessions = sessions;
        this.accounts = accounts;
    }

    @Override
    public int getBalance(Current __current) throws OperationException,
            SessionException, DbError {
        //printRequest(__current);
        //System.out.println("AccountImpl.getBalance()");
        String sid = __current.ctx.get("sid");
        AccountData acc = getAccount(sid);
        int balance = acc.getAmount();
        freeAccount(acc);
        return balance;
    }

    @Override
    public void withdraw(int amount, Current __current)
            throws OperationException, SessionException, DbError {
        //printRequest(__current);
        //System.out.println("AccountImpl.withdraw()");
        String sid = __current.ctx.get("sid");
        AccountData acc = getAccount(sid);
        acc.withdraw(amount);
        freeAccount(acc);
    }

    @Override
    public void deposit(int amount, Current __current)
            throws OperationException, SessionException, DbError {
        //printRequest(__current);
        //System.out.println("AccountImpl.deposit()");
        String sid = __current.ctx.get("sid");
        AccountData acc = getAccount(sid);
        acc.deposit(amount);
        freeAccount(acc);
    }
    
    @SuppressWarnings("unused")
    private static void printRequest(Current curr) {
        StringBuilder sb = new StringBuilder();
        sb.append("Working as " + curr.id.name);
        System.out.println(sb);
    }
    
    private AccountData getAccount(String sid) throws SessionExpired, DbError {
        Session session = sessions.getSessionById(sid);
        if (session != null) {
            return accounts.lockAccount(session.getUser());
        } else {
            throw new SessionExpired();
        }
    }
    
    private void freeAccount(AccountData account) throws DbError {
        accounts.unlockAccount(account);
    }

    @Override
    public String login(String password, Current __current) throws DbError,
            LoginException {
        throw new LoginException();
    }

    @Override
    public void logout(Current __current)
            throws SessionException {
        String sid = __current.ctx.get("sid");
        System.out.printf("[Bank] Logout (sid=%s)\n", sid);
        synchronized (sessions) {
            if (sessions.checkSessionActive(sid)) {
                sessions.removeSession(sid, RemovalReason.LOGGED_OUT);
            }
        }
    }
    

}
