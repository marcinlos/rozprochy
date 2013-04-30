package rozprochy.lab4.chat.server;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.generic.Session;
import Chat.Callback_Member_newMessage;
import Chat.MemberPrx;
import Chat.Message;
import Chat.NotAMember;
import Chat._RoomDisp;
import Ice.Current;
import Ice.LocalException;
import Users.InvalidSession;
import Users.SessionException;

public class RoomImpl extends _RoomDisp {
    
    private RoomManager manager;
    private BiSessionManager sessions;
    private RoomDatabase database;
    private String name;
    
    private Set<String> users = new HashSet<String>();
    
    private final String prefix;

    public RoomImpl(String name, RoomManager manager, RoomDatabase database, 
            BiSessionManager sessions) {
        this.name = name;
        this.manager = manager;
        this.database = database;
        this.sessions = sessions;
        this.prefix = "[Chat, " + name + "] ";
        System.out.println(prefix + "Room '" + name + "' created");
    }
    
    public String getName() {
        return name;
    }
    
    public void userSessionTerminated(BiSession session, RemovalReason reason) {
        String user = session.getUser();
        System.out.println(prefix + "User " + user + " is gone (" + reason + ")");
        synchronized (users) {
            users.remove(user);
        }
    }

    @Override
    public void join(String sessionId, Current __current)
            throws SessionException {
        BiSession session = sessions.getSessionById(sessionId);
        if (session == null) {
            throw new InvalidSession();
        }
        String user = session.getUser();
        System.out.println(prefix + "User " + user + " joined");
        manager.addUserRoom(user, name);
        // TODO Inform everyone
        synchronized (users) {
            users.add(user);
        }
        session.getCallback().greet("Welcome in " + name + "!");
        Message[] pending = database.getPending(user);
        if (pending != null && pending.length > 0) { 
            System.out.println("Delivering pending messages");
            session.getCallback().newMultipleMessages(pending);
            long lastId = pending[pending.length - 1].id;
            database.updateUser(user, lastId);
        }
    }

    @Override
    public void leave(String sessionId, Current __current) throws NotAMember,
            SessionException {
        Session session = sessions.getSessionById(sessionId);
        if (session == null) {
            throw new InvalidSession();
        }
        String user = session.getUser();
        synchronized (users) {
            if (users.contains(user)) {
                System.out.println(prefix + "User " + user + " has left");
                manager.removeUserRoom(user, name);
                // TODO Inform everyone
                users.remove(user);
                // TODO unsupported for now
                //database.removeUser(user);
            } else {
                throw new NotAMember();
            }
        }
    }
    
    @Override
    public void sendMessage(String sessionId, String text, Current __current)
            throws NotAMember, SessionException {
        Session session = sessions.getSessionById(sessionId);
        if (session == null) {
            throw new InvalidSession();
        }
        String user = session.getUser();
        final Message message = new Message();
        message.author = user;
        message.timestamp = new Date().getTime();
        message.content = text;
        message.room = name;
        database.addMessage(message);
        // TODO Inform everyone
        synchronized (users) {
            for (final String u: users) {
                BiSession s = sessions.getSessionByUser(u);
                MemberPrx cb = s.getCallback();
                if (cb != null) {
                    /*try {
                        cb.newMessage(message);
                        database.updateUser(u, message.id);
                    } catch (Ice.SocketException e) {
                        System.err.println("Socket exc");
                    } catch (Ice.TimeoutException e) {
                        System.err.println("Timeout exc");
                    }*/
                    cb.begin_newMessage(message, new Callback_Member_newMessage() {
                        
                        @Override
                        public void response() {
                            database.updateUser(u, message.id);
                        }
                        
                        @Override
                        public void exception(LocalException ex) {
                            try {
                                throw ex;
                            } catch (Ice.SocketException e) {
                                System.err.println("Socket exc");
                            } catch (Ice.TimeoutException e) {
                                System.err.println("Timeout exc");
                            }    
                        }
                    });
                }
            }
        }
    }

    @Override
    public Message[] fetchMessages(long since, Current __current)
            throws NotAMember, SessionException {
        return database.readSince(since);
    }

}
