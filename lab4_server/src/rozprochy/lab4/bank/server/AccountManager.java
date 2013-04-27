package rozprochy.lab4.bank.server;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import rozprochy.lab4.bank.util.Crypto;
import rozprochy.lab4.util.DiskMap;
import rozprochy.lab4.util.StringUtil;
import Bank.AccountAlreadyExists;

public class AccountManager {

    private static final int SALT_BYTES = 32;
    
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
                AccountData account = generateAccount(pesel, password);
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
                byte[] hashed = account.getHashed();
                byte[] salt = account.getSalt();
                byte[] value = computeHash(password, salt);
                for (int i = 0; i < value.length; ++ i) {
                    if (hashed[i] != value[i]) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }
    
    private static byte[] computeHash(String password, byte[] salt) {
        MessageDigest hasher;
        try {
            hasher = MessageDigest.getInstance("SHA-256");
            hasher.update(salt);
            hasher.update(StringUtil.encode(password));
            return hasher.digest();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No SHA-256 provider available");
            throw new RuntimeException(e);
        }
    }
    
    public AccountData generateAccount(String login, String password) {
        byte[] salt = Crypto.randomBytes(SALT_BYTES);
        byte[] hashed = computeHash(password, salt);
        return new AccountData(login, 0, hashed, salt);
    }

}
