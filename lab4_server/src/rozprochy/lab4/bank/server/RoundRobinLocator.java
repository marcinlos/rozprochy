package rozprochy.lab4.bank.server;

import java.util.ArrayList;
import java.util.List;

import Bank.InvalidSession;
import Bank._AccountDisp;
import Ice.Current;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.ServantLocator;
import Ice.UserException;

public class RoundRobinLocator implements ServantLocator {

    private SessionManager sessions;
    private List<_AccountDisp> servants = new ArrayList<_AccountDisp>();
    
    public RoundRobinLocator(SessionManager sessions) {
        this.sessions = sessions;
        System.out.println("Servant locator created");
    }

    @Override
    public Object locate(Current curr, LocalObjectHolder cookie)
            throws UserException {
        logRequest(curr);
        String session = curr.ctx.get("session");
        if (sessions.isSessionActive(session)) {
            return getNext();
        } else {
            throw new InvalidSession();
        }
    }

    @Override
    public void finished(Current curr, Object servant, java.lang.Object cookie)
            throws UserException {
        System.out.println("After request " + curr.requestId);
    }

    @Override
    public void deactivate(String category) {

    }
    
    private synchronized _AccountDisp getNext() {
        // TODO: Load balancing
        return servants.get(0);
    }
    
    private void logRequest(Current curr) {
        String adapter = curr.adapter.getName();
        int reqId = curr.requestId;
        String category = curr.id.category;
        String name = curr.id.name;
        String operation = curr.operation;
        String con = curr.con._toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Adapter: ").append(adapter).append("\n")
          .append("Request ID: ").append(reqId).append("\n")
          .append("Target: ").append(category).append(":").append(name)
          .append("\n")
          .append("Operation: ").append(operation).append("\n")
          .append("Connection: ").append(con).append("\n");
        System.out.println(sb.toString());
    }

}
