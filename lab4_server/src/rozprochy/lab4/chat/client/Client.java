package rozprochy.lab4.chat.client;

import static rozprochy.lab4.util.Console.BLUE;
import static rozprochy.lab4.util.Console.COLORS;
import static rozprochy.lab4.util.Console.GREEN;
import static rozprochy.lab4.util.Console.RESET;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import rozprochy.lab4.cli.CommandInterpreter;
import Chat.MemberPrx;
import Chat.MemberPrxHelper;
import Chat.Message;
import Chat.NeedForRecovery;
import Chat.NotAMember;
import Chat.RoomPrx;
import Chat.RoomPrxHelper;
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
    private String roomName;
    private volatile boolean pingOk = true;
    
    private ObjectAdapter adapter;
    private _MemberDisp callback;
    
    private String prefix;
    private String chatEndpoint;
    private String managerName;
    
    private Timer timer;
    
    private String prompt = "> ";
    
    private static final int PING_PERIOD = 5000;
    
    @Override
    public int run(String[] args) {
        loadProperties();
        ObjectPrx obj = makePrx(managerName).ice_timeout(1000);
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
        callback = new Callback();
        ObjectPrx obj = adapter.add(callback, id).ice_timeout(5000);
        MemberPrx proxy = MemberPrxHelper.uncheckedCast(obj);
        adapter.activate();
        chat.ice_getConnection().setAdapter(adapter);
        try {
            System.out.printf("Established bidir connection with timeout=%d ms\n",
                    chat.ice_getConnection().timeout());
            System.out.println(chat.ice_getConnection());
            chat.setCallback(sessionId, proxy);
        } catch (SessionException e) {
            System.err.println("Invalid session");
            printPrompt();
            invalidateSession();
        }
    }
    
    private void createPinger() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (sessionId != null) {
                    boolean wasOk = pingOk;
                    try {
                        pingOk = false;
                        chat.keepalive(sessionId);
                        pingOk = true;
                        if (! wasOk) {
                            System.out.println("\rConnection reestablished");
                            printPrompt();
                        }
                    } catch (Ice.TimeoutException e) {
                        System.err.println("\rConnection problem (timeout)");
                        printPrompt();
                    } catch (Ice.ConnectFailedException e) {
                        System.err.println("\rConnection failed: " + e.getMessage());
                        printPrompt();
                    } catch (SessionException e) {
                        System.err.println("\rPing: Invalid session"); 
                        System.err.println("Please log in");
                        printPrompt();
                        invalidateSession();
                        pingOk = true;
                    } catch (NeedForRecovery e) {
                        System.out.println("\rNeed to reestablish connection");
                        createCallback();
                    } 
                }
            }
        }, 0, PING_PERIOD);
    }
    
    private ObjectPrx makePrx(String name) {
        String str = prefix + "/" + name + chatEndpoint;
        return communicator().stringToProxy(str);
    }
    
    private void invalidateSession() {
        sessionId = null;
    }
    
    private RoomPrx getRoom(String name) {
        if (sessionId != null) {
            String str = "Room/" + name + chatEndpoint;
            ObjectPrx proxy = communicator().stringToProxy(str);
            return RoomPrxHelper.checkedCast(proxy);
        } else {
            return null;
        }
    }
    
    private String colorForLogin(String login) {
        int h = betterHash(login);
        int color = h % (COLORS.length - 1);
        return COLORS[color + 1];
    }
    
    private class Callback extends _MemberDisp {
        @Override
        public void userLeft(String room, String login, Current __current) {
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
            String dateStr = df.format(date);
            String col = colorForLogin(login);
            String text = String.format("\r%s[%s] %s %sUser %s%s%s has " +
                    "left the room", GREEN, room, dateStr, RESET, col, 
                    login, RESET);
            System.out.println(text);
            printPrompt();
        }

        @Override
        public void userJoined(String room, String login, Current __current) {
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
            String dateStr = df.format(date);
            String col = colorForLogin(login);
            String text = String.format("\r%s[%s] %s %sUser %s%s%s has " +
                    "joined the room", GREEN, room, dateStr, RESET, col, 
                    login, RESET);
            System.out.println(text);
            printPrompt();
        }

        @Override
        public void newMultipleMessages(Message[] msgs, Current __current) {
            for (Message msg: msgs) {
                printMessage(msg);
            }
            printPrompt();
        }

        @Override
        public void newMessage(Message msg, Current __current) {
            printMessage(msg);
            printPrompt();
        }

        @Override
        public void greet(String greeting, Current __current) {
            System.out.println(greeting);
        }
    }


    private void printPrompt() {
        StringBuilder sb = new StringBuilder('\r');
        sb.append(BLUE);
        if (roomName != null) {
            sb.append(roomName);
        }
        sb.append(prompt).append(RESET);
        System.out.print(sb);
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
        cli.registerHandler("usesid", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                try {
                    sessionId = input.next();
                } catch (NoSuchElementException e) {
                    System.err.println("Usage: usesid <sid>");
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
        cli.registerHandler("rooms", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        String[] rooms = chat.getRooms(sessionId);
                        System.out.println("Available rooms:");
                        for (String room: rooms) {
                            System.out.println("   " + room);
                        }
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                        invalidateSession();
                    }
                }
                printPrompt();
                return true;
            }
        });
        cli.registerHandler("join", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        String name = input.next();
                        System.out.println("Attempting to join " + name + "...");
                        RoomPrx room = getRoom(name);
                        room.join(sessionId);
                        System.out.println("Joined");
                        if (roomName == null) {
                            roomName = name;
                        }
                    } catch (NoSuchElementException e) {
                        System.err.println("Usage: join <name>");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                        invalidateSession();
                    } catch (Ice.ObjectNotExistException e) {
                        System.err.println("Room does not exist");
                    }
                }
                printPrompt();
                return true;
            }
        });
        cli.registerHandler("leave", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        String name = input.next();
                        System.out.println("Leaving " + name + "...");
                        RoomPrx room = getRoom(name);
                        room.leave(sessionId);
                        System.out.println("Left");
                    } catch (NoSuchElementException e) {
                        System.err.println("Usage: leave <name>");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                        invalidateSession();
                    } catch (NotAMember e) {
                        System.err.println("Wasn't a member of this room");
                    } catch (Ice.ObjectNotExistException e) {
                        System.err.println("Room does not exist");
                    } 
                }
                printPrompt();
                return true;
            }
        });
        cli.registerHandler("msg", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        String name = input.next();
                        if (input.hasNextLine()) {
                            RoomPrx room = getRoom(name);
                            room.sendMessage(sessionId, input.nextLine());
                        }
                    } catch (NoSuchElementException e) {
                        System.err.println("Usage: msg <room> <text...>");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                        invalidateSession();
                    } catch (NotAMember e) {
                        System.err.println("Wasn't a member of this room");
                    } catch (Ice.ObjectNotExistException e) {
                        System.err.println("Room does not exist");
                    } 
                }
                printPrompt();
                return true;
            }
        });
        cli.registerHandler("room", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        roomName = input.next();
                    } catch (NoSuchElementException e) {
                        System.err.println("Usage: room <name>");
                    }
                }
                printPrompt();
                return true;
            }
        });
        cli.registerHandler("fetch", new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    try {
                        String name = input.next();
                        long since = 0;
                        if (input.hasNextLong()) {
                            since = input.nextLong();
                        }
                        RoomPrx room = getRoom(name);
                        Message[] msgs = room.fetchMessages(since);
                        for (Message m: msgs) {
                            printMessage(m);
                        }
                    } catch (NoSuchElementException e) {
                        System.err.println("Usage: msg <room> <text...>");
                    } catch (SessionException e) {
                        System.err.println("Invalid session"); 
                        invalidateSession();
                    } catch (NotAMember e) {
                        System.err.println("Wasn't a member of this room");
                    } catch (Ice.ObjectNotExistException e) {
                        System.err.println("Room does not exist");
                    } 
                }
                printPrompt();
                return true;
            }
        });
        cli.setDefaultHandler(new IceCommand() {
            @Override public boolean doExecute(String cmd, Scanner input) {
                if (checkLogged()) {
                    if (roomName != null) {
                        try {
                            String message = cmd;
                            if (input.hasNextLine()) {
                                message += input.nextLine();
                            }
                            RoomPrx room = getRoom(roomName);
                            room.sendMessage(sessionId, message);
                        } catch (SessionException e) {
                            System.err.println("Invalid session"); 
                            invalidateSession();
                        } catch (NotAMember e) {
                            System.err.println("Wasn't a member of this room");
                        } catch (Ice.ObjectNotExistException e) {
                            System.err.println("Room does not exist");
                        } 
                    } else {
                        System.err.println("No room choosen");
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
    
    private int betterHash(String string) {
        int hash, i;
        for(hash = i = 0; i < string.length(); ++i)
        {
            hash += string.charAt(i);
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);
        return hash >>> 5;
    }
    
    private void printMessage(Message message) {
        Date date = new Date(message.timestamp);
        SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
        String dateStr = df.format(date);
        String col = colorForLogin(message.author);
        String text = String.format("\r%s[%s] %s %s%s%s %s", GREEN, 
                message.room, dateStr, col, message.author, RESET, 
                message.content);
        System.out.println(text);
    }
    
    private void exitGracefully() {
        System.out.print('\r');
        System.out.flush();
        timer.cancel();
        if (sessionId != null) {
            try {
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
