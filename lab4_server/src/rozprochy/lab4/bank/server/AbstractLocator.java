package rozprochy.lab4.bank.server;

import java.util.Map;

import rozprochy.lab4.generic.Session;
import rozprochy.lab4.generic.SessionManager;
import rozprochy.lab4.util.Crypto;
import Bank.OperationException;
import Bank._AccountDisp;
import Ice.Current;
import Ice.Identity;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.ServantLocator;
import Ice.UserException;
import Users.AuthenticationFailed;
import Users.DbError;
import Users.InvalidSession;
import Users.LoginException;
import Users.SessionException;

public abstract class AbstractLocator implements ServantLocator {

    protected SessionManager<Session> sessions;
    protected AccountManager accounts;
    protected Map<String, String> config;
    private boolean logCalls = false;
    
    protected static final String PREFIX = "[Bank] ";
    
    public AbstractLocator(SessionManager<Session> sessions, 
            AccountManager accounts, Map<String, String> config) {
        this.sessions = sessions;
        this.accounts = accounts;
        this.config = config;
        loadConfig();
    }
    
    private  void loadConfig() {
        System.out.println("Loading locator configuration");
        String val = config.get("BankApp.Locator.LogCalls");
        if (val != null) {
            logCalls = Boolean.valueOf(val);
        }
        System.out.println("   log calls? " + (logCalls ? "yes" : "no"));
    }
    
    
    protected int tryParseValue(String prop, int defaultValue) {
        String val = config.get(prop);
        if (val != null) {
            try {
                return Integer.valueOf(val);
            } catch (NumberFormatException e) {
                System.out.printf("   (invalid value '%s')'n", val);
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
    
    @Override
    public Object locate(Current curr, LocalObjectHolder cookie)
            throws UserException {
        if (logCalls) {
            logRequest(curr);
        }
        if (curr.operation.equals("login")) {
            return loggingServant;
        } else {
            String sid = curr.ctx.get("sid");
            return getServant(sid);
        }
    }

    protected abstract _AccountDisp getServant(String sid)
            throws SessionException;

    @Override
    public void finished(Current curr, Object servant, java.lang.Object cookie)
            throws UserException {
        if (logCalls) {
            System.out.println("After request " + curr.requestId);
        }
    }

    @Override
    public void deactivate(String category) {
        System.out.println("Locator deactivated");
    }
    
    private String idToString(Identity id) {
        return id.category + "/" + id.name;
    }
    
    protected void logRequest(Current curr) {
        String adapter = curr.adapter.getName();
        int reqId = curr.requestId;
        String operation = curr.operation;
        String con = curr.con._toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Adapter: ").append(adapter).append("\n")
          .append("Request ID: ").append(reqId).append("\n")
          .append("Target: ").append(idToString(curr.id)).append("\n")
          .append("Operation: ").append(operation).append("\n")
          .append("Connection:\n").append(con).append("\n");
        System.out.println(sb.toString());
    }
    
    protected _AccountDisp loggingServant = new _AccountDisp() {
        
        @Override
        public void withdraw(int amount, Current __current)
                throws OperationException, DbError, SessionException {
            throw new InvalidSession();
        }
        
        @Override
        public int getBalance(Current __current) throws OperationException,
                DbError, SessionException {
            throw new InvalidSession();
        }
        
        @Override
        public void deposit(int amount, Current __current)
                throws OperationException, DbError, SessionException {
            throw new InvalidSession();
        }

        @Override
        public String login(String password, Current __current)
                throws DbError, LoginException {
            String pesel = __current.id.name;
            return AbstractLocator.this.login(pesel, password);
        }

        @Override
        public void logout(Current __current)
                throws SessionException {
            throw new InvalidSession();
        }
        
    };

    protected String login(String pesel, String password) throws LoginException, 
    DbError {
        System.out.printf(PREFIX + "Login attempt (user=%s, pwd=%s)\n", 
                pesel, password);
        if (accounts.authenticate(pesel, password)) {
            String sid = Crypto.createSessionId();
            Session session = new Session(sid, pesel);
            sessions.addSession(session);
            System.out.printf(PREFIX + "Logged in (user=%s, pwd=%s)\n", 
                    pesel, password);
            return sid;
        } else {
            System.out.printf(PREFIX + "Authentication failed " +
                    "(user=%s, pwd=%s)\n", pesel, password);
            throw new AuthenticationFailed();
        }
    }

}
