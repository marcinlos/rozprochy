package rozprochy.lab4.generic;

import java.io.IOException;

import rozprochy.lab4.util.Crypto;
import Users.AccountAlreadyExists;
import Users.DbError;
import Users.InvalidLogin;
import Users.InvalidPassword;
import Users.RegisterException;

public abstract class AccountManager<T extends AccountData> {

    private static final int SALT_BYTES = 32;

    private String prefix;
    private AccountPersistor<T> accounts;

    public AccountManager(String prefix) {
        this.prefix = String.format("[%s] ", prefix);
    }
    
    protected void setPersistor(AccountPersistor<T> accounts) {
        this.accounts = accounts;
    }
    
    protected String prefix() {
        return prefix;
    }

    protected void validateLogin(String login) throws InvalidLogin {
        // empty
    }

    protected void validatePassword(String password) throws InvalidPassword {
        // empty
    }

    public void create(String login, String password, Object extra)
            throws RegisterException, DbError {
        validateLogin(login);
        validatePassword(password);
        accounts.lock();
        try {
            if (accounts.exists(login)) {
                System.out.printf(prefix + "Account already exists "
                        + "(user=%s)\n", login);
                throw new AccountAlreadyExists();
            } else {
                T account = generateAccount(login, password, extra);
                accounts.save(account);
                System.out.printf(prefix + "Account created (user=%s, "
                        + "pwd=%s)\n", login, password);
            }
        } catch (IOException e) {
            throw new DbError(e);
        } finally {
            accounts.unlock();
        }
    }

    public T lockAccount(String login) throws DbError {
        try {
            return accounts.lock(login);
        } catch (IOException e) {
            throw new DbError(e);
        }
    }

    public void unlockAccount(T account) throws DbError {
        try {
            accounts.commit(account);
        } catch (IOException e) {
            throw new DbError();
        }
    }

    public boolean authenticate(String login, String password) throws DbError {
        T account = lockAccount(login);
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

    public T generateAccount(String login, String password,
            Object extra) {
        byte[] salt = Crypto.randomBytes(SALT_BYTES);
        byte[] hashed = Crypto.computeHash(password, salt);
        return buildAccount(login, salt, hashed, extra);
    }

    protected abstract T buildAccount(String login, byte[] salt,
            byte[] hashedPassword, Object extra);

}
