package rozprochy.lab4.chat.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Ice.Current;
import Ice.LocalObjectHolder;
import Ice.Object;
import Ice.ServantLocator;
import Ice.UserException;

public class RoomManager implements ServantLocator {
    
    private Map<String, RoomImpl> rooms = new HashMap<String, RoomImpl>();
    
    // Map with rooms each player is subscribed to
    private Map<String, Set<String>> users = new HashMap<String, Set<String>>();
    
    private Map<String, String> config;

    
    
    public RoomManager(Map<String, String> config) {
        this.config = config;
    }
    
    
    public void addUserRoom(String user, String room) {
        synchronized (users) {
            Set<String> set = users.get(user);
            if (set == null) {
                set = new HashSet<String>();
            }
            set.add(room);
            users.put(user, set);
        }
    }
    
    
    public void removeUserRoom(String user, String room) {
        synchronized (user) {
            Set<String> set = users.get(user);
            if (set == null) {
                set = new HashSet<String>();
            }
            set.remove(room);
            users.put(user, set);
        }
    }

    @Override
    public Object locate(Current curr, LocalObjectHolder cookie)
            throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void finished(Current curr, Object servant, java.lang.Object cookie)
            throws UserException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deactivate(String category) {
        // TODO Auto-generated method stub
        
    }

}
