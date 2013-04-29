package rozprochy.lab4.chat.server;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import rozprochy.lab4.bank.util.PESEL;
import rozprochy.lab4.generic.AccountPersistor;
import rozprochy.lab4.generic.DiskMapPersistor;
import Users.InvalidLogin;
import Users.InvalidPassword;

public class AccountManager extends
        rozprochy.lab4.generic.AccountManager<ChatAccountData> {

    public AccountManager(String prefix, Map<String, String> config) {
        super(prefix);
        System.out.println(prefix() + "Initiating account manager");
        try {
            String dir = config.get("BankApp.Accounts.Dir");
            if (dir == null) {
                System.err.println(prefix() + "Warning: account storage " +
                        "directory not specified, using default");
                dir = ".bankdb";
            }
            AccountPersistor<ChatAccountData >accounts = 
                    new DiskMapPersistor<ChatAccountData>(new File(dir));
            System.out.println(prefix() + "Initialized storage in " + dir);
            System.out.printf(prefix() + "   Found %d account(s)\n", 
                    accounts.size());
            setPersistor(accounts);
        } catch (IOException e) {
            throw new RuntimeException(prefix() + "Failed to initialize " +
                    "persistent storage", e);
        }
        System.out.println(prefix() + "Account manager activated");
    }
    
    @Override
    protected void validateLogin(String login) throws InvalidLogin {
        if (! PESEL.validate(login)) {
            throw new InvalidLogin("Not a valid PESEL");
        }
    }
    
    @Override
    protected void validatePassword(String password) throws InvalidPassword {
        if (password.isEmpty()) {
            throw new InvalidPassword("Empty password not allowed");
        }
    }

    @Override
    protected ChatAccountData buildAccount(String login, byte[] salt,
            byte[] hashedPassword, Object extra) {
        return new ChatAccountData(login, hashedPassword, salt);
    }

}
