package rozprochy.lab4.bank.client;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import rozprochy.lab4.cli.Command;
import rozprochy.lab4.cli.CommandInterpreter;
import Bank.AccountPrx;
import Bank.AccountPrxHelper;
import Bank.AuthenticationFailed;
import Bank.LoginException;
import Bank.MultiLogin;
import Bank.OperationException;
import Bank.RegisterException;
import Bank.SessionException;
import Bank.SystemManagerPrx;
import Bank.SystemManagerPrxHelper;
import Ice.ObjectPrx;
import Ice.Properties;

public class Client extends Ice.Application {

    private SystemManagerPrx bank;
    private String sessionId;
    
    private String prefix;
    private String bankEndpoint;
    private String managerName;
    
    
    @Override
    public int run(String[] args) {
        loadProperties();
        ObjectPrx obj = makePrx(managerName);
        System.out.print("Obtaining bank reference...");
        System.out.flush();
        bank = SystemManagerPrxHelper.checkedCast(obj);
        System.out.println("done");
        try {
            repl();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return 0;
    } 
    
    private void loadProperties() {
        Properties prop = communicator().getProperties();
        bankEndpoint = prop.getProperty("Bank.Endpoints");
        prefix = prop.getProperty("Bank.Prefix");
        managerName = prop.getProperty("Bank.Name");
    }
    
    private ObjectPrx makePrx(String name) {
        String str = prefix + "/" + name + bankEndpoint;
        return communicator().stringToProxy(str);
    }
    
    private AccountPrx getAccount() {
        if (sessionId != null) {
            ObjectPrx proxy = makePrx(sessionId);
            return AccountPrxHelper.checkedCast(proxy);
        } else {
            return null;
        }
    }
    
    private abstract class IceCommand implements Command {

        public abstract boolean doExecute(String cmd, Scanner input);
        
        @Override
        public boolean execute(String cmd, Scanner input) {
            try {
                return doExecute(cmd, input);
            } catch (Ice.LocalException e) {
                System.err.println("Connection problem");
                e.printStackTrace(System.err);
                return true;
            }
        }
        
    }
    
    private void repl() throws IOException {
        CommandInterpreter cli = new CommandInterpreter();
        cli.registerHandler("register", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                try {
                    String pesel = input.next();
                    String password = input.next();
                    bank.createAccount(pesel, password);
                    System.out.println("Account successfully created");
                } catch (NoSuchElementException e) {
                    System.err.println("Usage: register <pesel> <password>");
                } catch (RegisterException e) {
                    System.err.println(e.getMessage());
                }
                return true;
            }
        });
        cli.registerHandler("login", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                try {
                    String pesel = input.next();
                    String password = input.next();
                    sessionId = bank.login(pesel, password);
                    System.out.println("Logged in, sid=" + sessionId);
                } catch (NoSuchElementException e) {
                    System.err.println("Usage: login <pesel> <password>");
                } catch (AuthenticationFailed e) {
                    System.err.println("Invalid login or password");
                } catch (MultiLogin e) {
                    System.err.println("User already logged in");
                } catch (LoginException e) {
                    e.printStackTrace(System.err);
                }
                return true;
            }
        });
        cli.registerHandler("logout", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (sessionId != null) {
                    try {
                        bank.logout(sessionId);
                        sessionId = null;
                        System.out.println("Logged out");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                    }
                } else {
                    System.err.println("Not logged in!");
                }
                return true;
            }
        });
        cli.registerHandler("balance", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        AccountPrx account = getAccount();
                        int balance = account.getBalance();
                        System.out.printf("Account : %10d.00 $\n", balance);
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                    } catch (OperationException e) {
                        System.out.println("Operation exception");
                    }
                }
                return true;
            }
        });
        cli.registerHandler("deposit", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        int amount = input.nextInt();
                        AccountPrx account = getAccount();
                        account.deposit(amount);
                    } catch (NumberFormatException e) {
                        System.err.println("Usage: deposit <amount>");
                    } catch (NoSuchElementException e) {
                        System.err.println("Usage: deposit <amount>");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                    } catch (OperationException e) {
                        System.out.println("Operation exception");
                    }
                }
                return true;
            }
        });
        cli.registerHandler("withdraw", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        int amount = input.nextInt();
                        AccountPrx account = getAccount();
                        account.withdraw(amount);
                    } catch (NumberFormatException e) {
                        System.err.println("Usage: withdraw <amount>");
                    } catch (NoSuchElementException e) {
                        System.err.println("Usage: withdraw <amount>");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                    } catch (OperationException e) {
                        System.out.println("Operation exception");
                    }
                }
                return true;
            }
        });
        cli.run();
    }
    
    private boolean checkLogged() {
        if (sessionId == null) {
            System.err.println("Not logged in!");
            return false;
        } else {
            return true;
        }
    }
    
    public static void main(String[] args) {
        Client client = new Client();
        client.main("BankClient", args);
    }

}
