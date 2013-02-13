package rozprochy.rok2011.lab2.zad2.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface of a subscriber client - allows notyfing about messages.
 */
public interface Subscriber extends Remote {

    /**
     * @param message message sent by the publisher
     */
    void notifyMessage(Message message) throws RemoteException;
    
    /**
     * Called by the server when new publisher with topic matching the one
     * with which this subscriber has registered itself appears. Upon receiving
     * this notification, subscriber may subscribe to this new publisher,
     * if it wishes to receive his messages.
     * 
     * @param publisher new publisher with matching topic
     */
    void newPublisher(Publisher publisher) throws RemoteException;
    
    /**
     * Invoked when a publisher to whom the client has been subscribed ceases
     * to send messages to it, e.g. because it finishes its execution.
     * 
     * @param publisher publisher who ceases to publish
     */
    void notifyUnsubscribed(Publisher publisher) throws RemoteException;
    
}
