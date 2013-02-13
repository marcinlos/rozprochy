package rozprochy.rok2011.lab2.zad2.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface of a publisher client - allows subscribing (to a particular 
 * topic, since a client is allowed to publish on just a single topic),
 */
public interface Publisher extends Remote {

    /**
     * Adds {@code client} to the list of entities notified about all the
     * messages published in future 
     */
    void subscribe(Subscriber client) throws RemoteException;

    /**
     * Removes a {@code client} from the list of subscribers
     */
    void unsubscribe(Subscriber client) throws RemoteException;

}