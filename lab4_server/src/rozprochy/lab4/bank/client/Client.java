package rozprochy.lab4.bank.client;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rozprochy.lab4.cli.Command;
import rozprochy.lab4.cli.CommandInterpreter;
import Bank.AccountPrx;
import Bank.AccountPrxHelper;
import Bank.OperationException;
import Bank.SystemManagerPrx;
import Bank.SystemManagerPrxHelper;
import Ice.ObjectPrx;
import Ice.Properties;
import Users.AuthenticationFailed;
import Users.DbError;
import Users.InvalidLogin;
import Users.InvalidPassword;
import Users.LoginException;
import Users.MultiLogin;
import Users.RegisterException;
import Users.SessionException;

public class Client extends Ice.Application {

    private SystemManagerPrx bank;
    private volatile String sessionId;
    
    private String prefix;
    private String bankEndpoint;
    private String managerName;
    
    private Timer timer;
    
    private String prompt = "> ";
    
    private static final char[] BUSY = {
        '-', '\\', '|', '/', '-', '\\', '|', '/' 
    };
    
    private static final int PING_PERIOD = 1500;
    
    
    @Override
    public int run(String[] args) {
        loadProperties();
        ObjectPrx obj = makePrx(managerName);
        System.out.print("Obtaining bank reference...");
        System.out.flush();
        try {
            bank = SystemManagerPrxHelper.checkedCast(obj);
            System.out.println("done");
        } catch (Ice.ConnectionRefusedException e) {
            System.out.println("\nConnection refused, is server running?");
            return 1;
        }
        setInterruptHook(new Thread(new ShutdownHook()));
        createPinger();
        try {
            repl();
            exitGracefully();
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
    
    private void createPinger() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (sessionId != null) {
                    try {
                        bank.keepalive(sessionId);
                    } catch (Ice.ConnectFailedException e) {
                        System.err.println("Connection failed: " + e.getMessage());
                    } catch (SessionException e) {
                        System.err.println("Pinger: session exception");
                    } 
                }
            }
        }, 0, PING_PERIOD);
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
    
    private void invalidateSession() {
        sessionId = null;
    }
    
    private abstract class IceCommand implements Command {

        public abstract boolean doExecute(String cmd, Scanner input);
        
        @Override
        public boolean execute(String cmd, Scanner input) {
            try {
                return doExecute(cmd, input);
            } catch (Ice.ConnectFailedException e) {
                System.err.println("Connection failed: " + e.getMessage());
            } catch (Ice.LocalException e) {
                System.err.println("Ice problem");
                e.printStackTrace(System.err);
            }
            return true;
        }
        
    }
    
    private void printPrompt() {
        System.out.print(prompt);
        System.out.flush();
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
                } catch (InvalidLogin e) {
                    System.err.println("Invalid login: " + e.reason);
                } catch (InvalidPassword e) {
                    System.err.println("Invalid password: " + e.reason);
                } catch (RegisterException e) {
                    e.printStackTrace(System.err);
                } catch (DbError e) {
                    System.err.println("Server database error");
                }
                printPrompt();
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
                } catch (DbError e) {
                    System.err.println("Server database error");
                }
                printPrompt();
                return true;
            }
        });
        cli.registerHandler("logout", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        bank.logout(sessionId);
                        sessionId = null;
                        System.out.println("Logged out");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                        invalidateSession();
                    }
                }
                printPrompt();
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
                        invalidateSession();
                    } catch (OperationException e) {
                        System.err.println("Operation exception");
                    } catch (DbError e) {
                        System.err.println("Server database error");
                    }
                }
                printPrompt();
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
                        invalidateSession();
                    } catch (OperationException e) {
                        System.err.println("Operation exception");
                    } catch (DbError e) {
                        System.err.println("Server database error");
                    }
                }
                printPrompt();
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
                        invalidateSession();
                    } catch (OperationException e) {
                        System.err.println("Operation exception");
                    } catch (DbError e) {
                        System.err.println("Server database error");
                    }
                }
                printPrompt();
                return true;
            }
        });
        cli.registerHandler("spam", new IceCommand() {
            @Override 
            public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        int count = input.nextInt();
                        int perSec = input.nextInt();
                        if (perSec <= 0) {
                            System.out.println("Error: nonpositive intensity");
                            return true;
                        } 
                        int delay = 1000 / perSec;
                        long begin = System.currentTimeMillis();
                        int n = 0;
                        AccountPrx account = getAccount();
                        int remaining = count;
                        try {
                            while (-- remaining != 0) {
                                // Deliberately almost-infinite when count <= 0
                                TimeUnit.MILLISECONDS.sleep(delay);
                                account.getBalance();
                                long now = System.currentTimeMillis();
                                if (now - begin > 100) {
                                    begin = now;
                                    if (remaining < 0) {
                                        n = (n + 1) % BUSY.length;
                                        System.out.print("\rSpamming... " + BUSY[n]);
                                    } else {
                                        double done = count - remaining;
                                        double p = done / count * 100;
                                        System.out.printf("\rSpamming... %3.0f%%", p);
                                    }
                                    System.out.flush();
                                }
                            }
                            System.out.println("\rSpamming finished");
                        } catch (InterruptedException e) {
                            System.out.println("Interrupted");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Usage: spam <count> <calls/s>");
                    } catch (NoSuchElementException e) {
                        System.err.println("Usage: withdraw <amount>");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                        invalidateSession();
                    } catch (OperationException e) {
                        System.err.println("Operation exception");
                    } catch (DbError e) {
                        System.err.println("Server database error");
                    }
                }
                printPrompt();
                return true;
            }
        });
        printPrompt();
        cli.run();
    }
    
    private boolean checkLogged() {
        if (bank == null) {
            System.err.println("Not connected!");
            return false;
        } else if (sessionId == null) {
            System.err.println("Not logged in!");
            return false;
        } else {
            return true;
        }
    }
    
    private void exitGracefully() {
        System.out.print('\r');
        System.out.flush();
        timer.cancel();
        if (sessionId != null) {
            try {
                bank.logout(sessionId);
            } catch (SessionException e) {
                System.err.println("Invalid session"); 
            }
        }
    }
    
    private class ShutdownHook implements Runnable {
        
        @Override
        public void run() {
            System.out.print('\r');
            System.out.flush();
            timer.cancel();
            communicator().destroy();
        }
        
    }
    
    
    public static void main(String[] args) {
        Client client = new Client();
        client.main("BankClient", args);
    }

}
