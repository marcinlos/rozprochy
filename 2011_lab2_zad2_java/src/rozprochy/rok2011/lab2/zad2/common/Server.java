package rozprochy.rok2011.lab2.zad2.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Interface of a subscription server
 */
public interface Server extends Remote {
    
    /** Well-known name */
    public static final String NAME = "Server";

    /**
     * Registers subscriber with a list of topics of interest.
     * 
     * @return list of publishers currently publishing on topic mentioned
     * in {@code topics}
     */
    Collection<Publisher> registerSubscriber(Subscriber subscriber,
            Collection<String> topics) throws RemoteException;

    /**
     * Unregisteres a particular subscriber.
     */
    void unregisterSubscriber(Subscriber subscriber) throws RemoteException;

    
    void registerPublisher(Publisher publisher, String topic)
            throws RemoteException;

    
    void unregisterPublisher(Publisher publisher) throws RemoteException;

}
