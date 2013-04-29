package rozprochy.lab4.generic;

import java.io.IOException;

public interface AccountPersistor<T> {
    
    T load(String login) throws IOException;
    void save(T account) throws IOException;
    boolean exists(String login) throws IOException;
    
    T lock(String login) throws IOException;
    void unlock(String login);
    void commit(T account) throws IOException;
    
    void lock();
    void unlock();
    
    int size() throws IOException;
    
}
