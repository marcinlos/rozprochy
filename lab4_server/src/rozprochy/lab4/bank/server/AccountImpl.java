package rozprochy.lab4.bank.server;

import Bank.OperationException;
import Bank.SessionException;
import Bank.SessionExpired;
import Bank._AccountDisp;
import Ice.Current;

public class AccountImpl extends _AccountDisp {

    protected SessionManager sessions;
    protected AccountManager accounts;
    
    public AccountImpl(SessionManager sessions, AccountManager accounts) {
        this.sessions = sessions;
        this.accounts = accounts;
    }

    @Override
    public int getBalance(Current __current) throws OperationException,
            SessionException {
        //printRequest(__current);
        //System.out.println("AccountImpl.getBalance()");
        AccountData acc = getAccount(__current.id.name);
        int balance = acc.getAmount();
        freeAccount(acc);
        return balance;
    }

    @Override
    public void withdraw(int amount, Current __current)
            throws OperationException, SessionException {
        //printRequest(__current);
        //System.out.println("AccountImpl.withdraw()");
        AccountData acc = getAccount(__current.id.name);
        acc.withdraw(amount);
        freeAccount(acc);
    }

    @Override
    public void deposit(int amount, Current __current)
            throws OperationException, SessionException {
        //printRequest(__current);
        //System.out.println("AccountImpl.deposit()");
        AccountData acc = getAccount(__current.id.name);
        acc.deposit(amount);
        freeAccount(acc);
    }
    
    private static void printRequest(Current curr) {
        StringBuilder sb = new StringBuilder();
        sb.append("Working as " + curr.id.name);
        System.out.println(sb);
    }
    
    private AccountData getAccount(String sid) throws SessionExpired {
        Session session = sessions.getSessionById(sid);
        if (session != null) {
            return accounts.lockAccount(session.getUser());
        } else {
            throw new SessionExpired();
        }
    }
    
    private void freeAccount(AccountData account) {
        accounts.unlockAccount(account);
    }

}
