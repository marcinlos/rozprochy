package rozprochy.lab4.bank.server;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Semaphore;

import rozprochy.lab4.bank.util.PESEL;
import rozprochy.lab4.util.Crypto;
import rozprochy.lab4.util.DiskMap;
import Bank.AccountAlreadyExists;
import Bank.EmptyPassword;
import Bank.InvalidPesel;
import Bank.RegisterException;

public class AccountManager {

    private static final int SALT_BYTES = 32;
    
    private String dir;
    private Map<String, AccountData> accounts;
    
    private Object lock = new Object();
    private Semaphore dataLock = new Semaphore(1);
    
    public AccountManager(Map<String, String> config) {
        System.out.println("Initiating account manager");
        try {
            dir = config.get("BankApp.Accounts.Dir");
            if (dir == null) {
                System.err.println("Warning: account storage directory " +
                        "not specified, using default");
                dir = ".bankdb";
            }
            accounts = new DiskMap<AccountData>(new File(dir));
            System.out.println("Initialized storage in " + dir);
            System.out.printf("   Found %d account(s)\n", accounts.size());
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize persistent " + 
                    "storage", e);
        }
        System.out.println("Account manager activated");
    }


    public void create(String pesel, String password)
            throws RegisterException {
        if (! PESEL.validate(pesel)) {
            throw new InvalidPesel();
        } else if (password.isEmpty()) {
            throw new EmptyPassword();
        }
        synchronized (lock) {
            if (accounts.containsKey(pesel)) {
                System.out.printf("Account already exists (user=%s)\n", pesel);
                throw new AccountAlreadyExists();
            } else {
                AccountData account = generateAccount(pesel, password);
                accounts.put(pesel, account);
                System.out.printf("Account created (user=%s, pwd=%s)\n", 
                        pesel, password);
            }
        }
    }
    
    public AccountData lockAccount(String pesel) {
        try {
            dataLock.acquire();
            synchronized (lock) {
                AccountData acc = accounts.get(pesel);
                if (acc == null) {
                    dataLock.release();
                }
                return acc;
            }
        } catch (InterruptedException e) {
            System.err.println("Account manager interrupted");
            throw new RuntimeException(e);
        }
    }
    
    public void unlockAccount(AccountData account) {
        String pesel = account.getOwner();
        if (accounts.containsKey(pesel)) {
            accounts.put(pesel, account);
        }
        dataLock.release();
    }

    public boolean authenticate(String pesel, String password) {
        synchronized (lock) {
            AccountData account = lockAccount(pesel);
            if (account != null) {
                try {
                    byte[] hashed = account.getHashed();
                    byte[] salt = account.getSalt();
                    byte[] value = Crypto.computeHash(password, salt);
                    return Crypto.compareDigests(hashed, value);
                } finally {
                    unlockAccount(account);
                }
            } else {
                return false;
            }
        }
    }
    
    public AccountData generateAccount(String login, String password) {
        byte[] salt = Crypto.randomBytes(SALT_BYTES);
        byte[] hashed = Crypto.computeHash(password, salt);
        return new AccountData(login, 0, hashed, salt);
    }

}
