package rozprochy.lab2.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameListener extends Remote {

    void otherJoined(String nick) throws RemoteException;
    
    void gameUpdated(GameUpdate update) throws RemoteException;

    void gameEnded(GameResult result) throws RemoteException;

}
