package rozprochy.lab4.generic;

/**
 * Interface for obtaining notifications about session events. Main purpose
 * is to enable servant locators to non-intrusively observe session removal.
 * 
 * @author los
 */
public interface SessionListener<T extends Session> {
    
    /**
     * Invoked for each session removed at the session manager.
     * 
     * @param removed session
     * @param reason Why was the session removed
     */
    void sessionRemoved(T session, RemovalReason reason);
    
}
