package rozprochy.lab4.bank.server;

import Bank.OperationException;
import Bank.SessionException;
import Bank._AccountDisp;
import Ice.Current;

public class AccountImpl extends _AccountDisp {

    public AccountImpl() {
        // TODO Auto-generated constructor stub
        System.out.println("AccountImpl.<init>()");
    }

    @Override
    public int getBalance(Current __current) throws OperationException,
            SessionException {
        printRequest(__current);
        // TODO Auto-generated method stub
        System.out.println("AccountImpl.getBalance()");
        return 0;
    }

    @Override
    public void withdraw(int amount, Current __current)
            throws OperationException, SessionException {
        printRequest(__current);
        // TODO Auto-generated method stub
        System.out.println("AccountImpl.withdraw()");

    }

    @Override
    public void deposit(int amount, Current __current)
            throws OperationException, SessionException {
        printRequest(__current);
        // TODO Auto-generated method stub
        System.out.println("AccountImpl.deposit()");

    }
    
    private static void printRequest(Current curr) {
        StringBuilder sb = new StringBuilder();
        sb.append("Working as " + curr.ctx.get("session"));
    }

}
