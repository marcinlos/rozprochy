package rozprochy.lab4.chat.server;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import rozprochy.lab4.generic.RemovalReason;
import rozprochy.lab4.generic.Session;
import Chat.Callback_Member_newMessage;
import Chat.Callback_Member_userJoined;
import Chat.Callback_Member_userLeft;
import Chat.MemberPrx;
import Chat.Message;
import Chat.NotAMember;
import Chat._RoomDisp;
import Ice.Current;
import Ice.LocalException;
import Users.InvalidSession;
import Users.SessionException;
import Users.SessionExpired;

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
            for (String u: users) {
                notifyAboutLeave(user, u);
            }
        }
    }
    
    public boolean isEmpty() {
        synchronized (users) {
            return users.isEmpty();
        }
    }
    
    public void deactivate() {
        System.out.println(prefix + "Deactivation");
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
        synchronized (users) {
            for (String u: users) {
                notifyAboutJoin(user, u);
            }
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
                synchronized (users) {
                    users.remove(user);
                    for (String u: users) {
                        notifyAboutLeave(user, u);
                    }
                }
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
        Message message = new Message();
        message.author = user;
        message.timestamp = new Date().getTime();
        message.content = text;
        message.room = name;
        database.addMessage(message);
        synchronized (users) {
            for (String u: users) {
                notifyAboutMessage(message, u);
            }
        }
    }

    private void notifyAboutMessage(final Message message, final String user) {
        try {
            BiSession s = sessions.getSessionByUser(user);
            MemberPrx cb = s.getCallback();
            if (cb != null) {
                cb.begin_newMessage(message, new NewMessageCallback(message, user));
            }
        } catch (SessionExpired e) {
            // Swallow, nothing extraordinary
            System.out.printf("%sAttempt to notify timed-out user about " + 
                    "a new message(user=%s)\n", prefix, user);
        }
    }
    
    private void notifyAboutJoin(final String joined, final String user) {
        try {
            BiSession s = sessions.getSessionByUser(user);
            MemberPrx cb = s.getCallback();
            if (cb != null) {
                cb.begin_userJoined(name, joined, new UserJoinedCallback());
            }
        } catch (SessionExpired e) {
            // Swallow, nothing extraordinary
            System.out.printf("%sAttempt to notify timed-out user about " + 
                    "new room member (user=%s)\n", prefix, user);
        }
    }
    
    private void notifyAboutLeave(final String joined, final String user) {
        try {
            BiSession s = sessions.getSessionByUser(user);
            MemberPrx cb = s.getCallback();
            if (cb != null) {
                cb.begin_userLeft(name, joined, new UserLeftCallback());
            }
        } catch (SessionExpired e) {
            // Swallow, nothing extraordinary
            System.out.printf("%sAttempt to notify timed-out user about " + 
                    "departure of a room member (user=%s)\n", prefix, user);
        }
    }
    
    private class NewMessageCallback extends Callback_Member_newMessage {
        private final Message message;
        private final String user;

        private NewMessageCallback(Message message, String user) {
            this.message = message;
            this.user = user;
        }

        @Override public void response() {
            database.updateUser(user, message.id);
        }

        @Override public void exception(LocalException ex) {
            try {
                throw ex;
            } catch (Ice.SocketException e) {
                System.out.println(prefix + "User " + user + " has " + 
                        "connection problems, arranging for retry");
                arrangeRetry();
            } catch (Ice.TimeoutException e) {
                System.out.println(prefix + "User " + user + " has " + 
                        "socket timeout problems, arranging for retry");
                arrangeRetry();
            }
        }
        
        private void arrangeRetry() {
            try {
                BiSession s = sessions.getSessionByUser(user);
                s.addRecoveryListener(new SessionRecoveryListener() {
                    @Override
                    public void sessionRecovered(BiSession session) {
                        System.out.println(prefix + "User " + user + 
                                "has recovered, resending...");
                        notifyAboutMessage(message, user);
                    }
                });
            } catch (SessionExpired e) {
                // Swallow, nothing extraordinary
                System.out.printf("%sSession expired, no further resend " +
                        "attempts (user=%s)\n", prefix, user);
            }
        }
    }

    private class UserJoinedCallback extends Callback_Member_userJoined {
        @Override public void response() { 
            // empty
        }
        
        @Override public void exception(LocalException ex) {
            try {
                throw ex;
            } catch (Ice.SocketException e) {
                System.err.println("Socket exc");
            } catch (Ice.TimeoutException e) {
                System.err.println("Timeout exc");
            }
        }
    }
    
    private class UserLeftCallback extends Callback_Member_userLeft {
        @Override public void response() { 
            // empty
        }
        
        @Override public void exception(LocalException ex) {
            try {
                throw ex;
            } catch (Ice.SocketException e) {
                System.err.println("Socket exc");
            } catch (Ice.TimeoutException e) {
                System.err.println("Timeout exc");
            }
        }
    }

    @Override
    public Message[] fetchMessages(long since, Current __current)
            throws NotAMember, SessionException {
        return database.readSince(since);
    }

}
