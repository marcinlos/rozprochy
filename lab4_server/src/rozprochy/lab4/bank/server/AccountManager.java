package rozprochy.lab4.bank.server;

import java.util.HashMap;
import java.util.Map;

import Bank.AccountAlreadyExists;

public class AccountManager {

    private Map<String, AccountData> accounts = new HashMap<String, AccountData>();

    private Object lock = new Object();

    public void create(String pesel, String password)
            throws AccountAlreadyExists {

        synchronized (lock) {
            if (accounts.containsKey(pesel)) {
                throw new AccountAlreadyExists();
            } else {
                AccountData account = new AccountData(pesel, 0);
                accounts.put(pesel, account);
            }
        }
    }

    public boolean authenticate(String pesel, String password) {
        synchronized (lock) {
            AccountData account = accounts.get(pesel);
            if (account != null) {
                // TODO: Authenticate
                return true;
            } else {
                return false;
            }
        }
    }

}
