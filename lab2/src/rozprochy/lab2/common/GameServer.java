package rozprochy.lab2.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import rozprochy.lab2.common.exceptions.LoginException;

public interface GameServer extends Remote {
    
    public static final String NAME = "ships";
    
    Session login(String nick) throws RemoteException, LoginException;
    
    Collection<GameStatus> getGames() throws RemoteException;

}
