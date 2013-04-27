package rozprochy.lab4.bank.server;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import rozprochy.lab4.util.DiskMap;
import Bank.AccountAlreadyExists;

public class AccountManager {

    private String dir;
    private Map<String, AccountData> accounts;
    
    public AccountManager(Map<String, String> config) {
        System.out.println("Initiating account manager");
        try {
            dir = config.get("BankApp.Accounts.Dir");
            if (dir == null) {
                System.err.println("Warning: account storage directory " +
                        "not specified, using default");
                dir = ".data";
            }
            accounts = new DiskMap<AccountData>(new File(dir));
            System.out.println("Initialized storage in " + dir);
            System.out.printf("Found %d account(s)\n", accounts.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize persistent " + 
                    "storage", e);
        }
        System.out.println("Account manager activated");
    }

    private Object lock = new Object();

    public void create(String pesel, String password)
            throws AccountAlreadyExists {

        synchronized (lock) {
            if (accounts.containsKey(pesel)) {
                System.out.printf("Account already exists (user=%s)\n", pesel);
                throw new AccountAlreadyExists();
            } else {
                AccountData account = new AccountData(pesel, 0);
                accounts.put(pesel, account);
                System.out.printf("Account created (user=%s, pwd=%s)\n", 
                        pesel, password);
                
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
