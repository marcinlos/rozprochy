package rozprochy.lab4.generic;

/**
 * Interface for obtaining notifications about session events. Main purpose
 * is to enable servant locators to non-intrusively observe session removal.
 * 
 * @author los
 */
public interface SessionListener {
    
    /**
     * Invoked for each session removed at the session manager.
     * 
     * @param sid id of the removed session
     * @param reason Why was the session removed
     */
    void sessionRemoved(String sid, RemovalReason reason);
    
}
