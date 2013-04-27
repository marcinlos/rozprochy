package rozprochy.lab4.bank.server;

public class Session {
    
    private String id;
    private String user;
    
    /** 
     * When was the session last updated (time as returned by
     *  {@linkplain System#currentTimeMillis()}
     */
    transient volatile long lastUsage;

    public Session(String id, String user) {
        super();
        this.id = id;
        this.user = user;
        this.lastUsage = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    
    /**
     * Updates the last usage time
     */
    public void touch() {
        lastUsage = System.currentTimeMillis();
    }
    
    /**
     * @return time between now and last invocation of {@linkplain #touch()}
     */
    public long timeSinceUsed() {
        long now = System.currentTimeMillis();
        return now - lastUsage;
    }

}
