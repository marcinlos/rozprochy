package rozprochy.lab4.bank.server;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Semaphore;

import rozprochy.lab4.bank.util.PESEL;
import rozprochy.lab4.generic.AccountPersistor;
import rozprochy.lab4.generic.DiskMapPersistor;
import rozprochy.lab4.util.Crypto;
import Ice.IdentityParseException;
import Users.AccountAlreadyExists;
import Users.DbError;
import Users.InvalidLogin;
import Users.InvalidPassword;
import Users.RegisterException;

public class AccountManager {

    private static final int SALT_BYTES = 32;
    private final String prefix;
    
    private String dir;
    private AccountPersistor<AccountData> accounts;
    
    private Object lock = new Object();
    
    public AccountManager(String prefix, Map<String, String> config) {
        this.prefix = String.format("[%s] ", prefix);
        System.out.println(prefix + "Initiating account manager");
        try {
            dir = config.get("BankApp.Accounts.Dir");
            if (dir == null) {
                System.err.println(prefix + "Warning: account storage " +
                        "directory not specified, using default");
                dir = ".bankdb";
            }
            accounts = new DiskMapPersistor<AccountData>(new File(dir));
            System.out.println(prefix + "Initialized storage in " + dir);
            System.out.printf("   Found %d account(s)\n", accounts.size());
        } catch (IOException e) {
            throw new RuntimeException(prefix + "Failed to initialize " +
                    "persistent storage", e);
        }
        System.out.println("[Bank] Account manager activated");
    }

    protected void validateLogin(String login) throws InvalidLogin {
        if (! PESEL.validate(login)) {
            throw new InvalidLogin("Not a valid PESEL");
        }
    }
    
    protected void validatePassword(String password) throws InvalidPassword {
        if (password.isEmpty()) {
            throw new InvalidPassword("Empty password not allowed");
        }
    }

    public void create(String login, String password, Object extra)
            throws RegisterException, DbError {
        validateLogin(login);
        validatePassword(password);
        accounts.lock();
        try {
            if (accounts.exists(login)) {
                System.out.printf(prefix + "Account already exists " + 
                        "(user=%s)\n", login);
                throw new AccountAlreadyExists();
            } else {
                AccountData account = generateAccount(login, password, extra);
                accounts.save(account);
                System.out.printf(prefix + "Account created (user=%s, " + 
                        "pwd=%s)\n", login, password);
            }
        } catch (IOException e) {
            throw new DbError(e);
        } finally {
            accounts.unlock();
        }
    }
    
    public AccountData lockAccount(String login) throws DbError {
        try {
            return accounts.lock(login);
        } catch (IOException e) {
            throw new DbError(e);
        }
    }
    
    public void unlockAccount(AccountData account) throws DbError {
        try {
            accounts.commit(account);
        } catch (IOException e) {
            throw new DbError();
        }
    }

    public boolean authenticate(String login, String password) throws DbError {
        AccountData account = lockAccount(login);
        if (account != null) {
            try {
                byte[] hashed = account.getHashedPassword();
                byte[] salt = account.getSalt();
                return Crypto.authenticate(password, salt, hashed);
            } finally {
                unlockAccount(account);
            }
        } else {
            return false;
        }
    }
    
    public AccountData generateAccount(String login, String password, 
            Object extra) {
        byte[] salt = Crypto.randomBytes(SALT_BYTES);
        byte[] hashed = Crypto.computeHash(password, salt);
        return buildAccount(login, salt, hashed, extra);
    }
    
    protected AccountData buildAccount(String login, byte[] salt,
            byte[] hashedPassword, Object extra) {
        return new AccountData(login, 0, hashedPassword, salt);
    }

}
