package rozprochy.lab2.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import rozprochy.lab2.common.exceptions.JoinException;

public interface Session extends Remote {

    Game join(String room, long id, GameListener listener)
            throws RemoteException, JoinException;
    
    Game create(String room, GameListener listener) throws RemoteException;
    
    Game createWithBot(GameListener listener) throws RemoteException;

    void logout() throws RemoteException;

}
