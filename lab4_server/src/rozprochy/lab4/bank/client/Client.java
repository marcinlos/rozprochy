package rozprochy.lab4.bank.client;

import java.io.IOException;
import java.util.Scanner;

import rozprochy.lab4.cli.Command;
import rozprochy.lab4.cli.CommandInterpreter;
import Bank.AuthenticationFailed;
import Bank.LoginException;
import Bank.MultiLogin;
import Bank.RegisterException;
import Bank.SessionException;
import Bank.SystemManagerPrx;
import Bank.SystemManagerPrxHelper;
import Ice.ObjectPrx;

public class Client extends Ice.Application {

    private SystemManagerPrx bank;
    private String sessionId;
    
    @Override
    public int run(String[] args) {
        ObjectPrx obj = communicator().propertyToProxy("Bank.Proxy");
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
                String pesel = input.next();
                String password = input.next();
                try {
                    bank.createAccount(pesel, password);
                    System.out.println("Account successfully created");
                } catch (RegisterException e) {
                    System.err.println(e.getMessage());
                }
                return true;
            }
        });
        cli.registerHandler("login", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                String pesel = input.next();
                String password = input.next();
                try {
                    sessionId = bank.login(pesel, password);
                    System.out.println("Logged in, sid=" + sessionId);
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
                }
                return true;
            }
        });
        cli.registerHandler("", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                return true;
            }
        });
        cli.registerHandler("", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                return true;
            }
        });
        cli.registerHandler("", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                return true;
            }
        });
        cli.run();
    }
    
    public static void main(String[] args) {
        Client client = new Client();
        client.main("BankClient", args);
    }

}
