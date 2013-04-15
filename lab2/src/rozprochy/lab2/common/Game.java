package rozprochy.lab2.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import rozprochy.lab2.common.exceptions.MoveException;

public interface Game extends Remote {
    
    MoveResult move(GameMove move) throws RemoteException, MoveException;
    
    void leave() throws RemoteException;
    
}
