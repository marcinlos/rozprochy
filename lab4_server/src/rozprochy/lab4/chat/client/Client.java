package rozprochy.lab4.chat.client;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import rozprochy.lab4.cli.Command;
import rozprochy.lab4.cli.CommandInterpreter;
import Chat.MemberPrx;
import Chat.MemberPrxHelper;
import Chat.Message;
import Chat.NeedForRecovery;
import Chat.SystemManagerPrx;
import Chat.SystemManagerPrxHelper;
import Chat._MemberDisp;
import Ice.Current;
import Ice.Identity;
import Ice.ObjectAdapter;
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

    private SystemManagerPrx chat;
    private volatile String sessionId;
    
    private ObjectAdapter adapter;
    private _MemberDisp callback;
    
    private String prefix;
    private String chatEndpoint;
    private String managerName;
    
    private Timer timer;
    
    private String prompt = "> ";
    
    private static final int PING_PERIOD = 1500;
    
    
    @Override
    public int run(String[] args) {
        loadProperties();
        ObjectPrx obj = makePrx(managerName);
        System.out.print("Obtaining chat reference...");
        System.out.flush();
        try {
            chat = SystemManagerPrxHelper.checkedCast(obj);
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
        chatEndpoint = prop.getProperty("Chat.Endpoints");
        prefix = prop.getProperty("Chat.Prefix");
        managerName = prop.getProperty("Chat.Name");
    }
    
    private void createCallback() {
        adapter = communicator().createObjectAdapter("");
        String uuid = UUID.randomUUID().toString();
        Identity id = communicator().stringToIdentity(uuid);
        callback = new _MemberDisp() {
            @Override
            public void userLeaved(String room, String login, Current __current) {
            }
            @Override
            public void userJoined(String room, String login, Current __current) {
            }
            @Override
            public void newMultipleMessages(Message[] msgs, Current __current) {
            }
            @Override
            public void newMessage(Message msg, Current __current) {
            }
            @Override
            public void keepalive(Current __current) {
            }
            @Override
            public void greet(String greeting, Current __current) {
                System.out.println("Greeting received: " + greeting);
            }
        };
        ObjectPrx obj = adapter.add(callback, id);
        MemberPrx proxy = MemberPrxHelper.uncheckedCast(obj);
        adapter.activate();
        chat.ice_getConnection().setAdapter(adapter);
        try {
            chat.setCallback(sessionId, proxy);
        } catch (SessionException e) {
            System.err.println("Session fucked up");
        }
    }
    
    private void createPinger() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (sessionId != null) {
                    try {
                        chat.keepalive(sessionId);
                    } catch (Ice.ConnectFailedException e) {
                        System.err.println("Connection failed: " + e.getMessage());
                    } catch (SessionException e) {
                        System.err.println("Pinger: session exception");
                    } catch (NeedForRecovery e) {
                        // TODO Send recovery message 
                        e.printStackTrace();
                    } 
                }
            }
        }, 0, PING_PERIOD);
    }
    
    private ObjectPrx makePrx(String name) {
        String str = prefix + "/" + name + chatEndpoint;
        return communicator().stringToProxy(str);
    }

    /*
    private AccountPrx getAccount() {
        if (sessionId != null) {
            ObjectPrx proxy = makePrx(sessionId);
            return AccountPrxHelper.checkedCast(proxy);
        } else {
            return null;
        }
    }
    */
    
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
                    String login = input.next();
                    String password = input.next();
                    chat.createAccount(login, password);
                    System.out.println("Account successfully created");
                } catch (NoSuchElementException e) {
                    System.err.println("Usage: register <login> <password>");
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
                    String login = input.next();
                    String password = input.next();
                    sessionId = chat.login(login, password);
                    System.out.println("Logged in, sid=" + sessionId);
                    createCallback();
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
                        chat.logout(sessionId);
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
        printPrompt();
        cli.run();
    }
    
    private boolean checkLogged() {
        if (chat == null) {
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
                System.out.println("Quitting....");
                chat.logout(sessionId);
                adapter.destroy();
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
        client.main("ChatClient", args);
    }

}
