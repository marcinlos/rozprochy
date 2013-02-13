package rozprochy.rok2011.lab2.zad2.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import rozprochy.rok2011.lab2.zad2.common.Client;
import rozprochy.rok2011.lab2.zad2.common.Message;
import rozprochy.rok2011.lab2.zad2.common.Publisher;
import rozprochy.rok2011.lab2.zad2.common.Server;
import rozprochy.rok2011.lab2.zad2.common.Subscriber;


public class ClientImpl extends UnicastRemoteObject implements Client {

    private Server server;
    private String publishedTopic;
    //private Collection<String> subscribed;
    
    /** Clients subscribed to this client */
    private Set<Subscriber> subscribers = new HashSet<Subscriber>();
    
    /** Publishers to whom the client is subscribed */
    private Set<Publisher> publishers = new HashSet<Publisher>();
    
    
    /**
     * Creates the client object, notifies server and subscribes to relevant
     * publishers.
     */
    public ClientImpl(Server server, String published, 
            Collection<String> subscribed) throws RemoteException {
        this.server = server;
        this.publishedTopic = published;
        //this.subscribed = subscribed;
        
        server.registerPublisher(this, published);
        Collection<Publisher> pubs = server.registerSubscriber(this, subscribed);
        System.out.println("Registered, got publishers list");
        
        // Add all the new subscribers
        for (Publisher publisher : pubs) {
            newPublisher(publisher);
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized
    void subscribe(Subscriber client) throws RemoteException {
        // See newPublisher for discussion of this check
        if (! this.equals(client)) {
            System.out.println("New subscriber");
            subscribers.add(client);
        }
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized 
    void unsubscribe(Subscriber client) throws RemoteException {
        System.out.println("Client unsubscribed");
        subscribers.remove(client);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public
    void notifyMessage(Message message) throws RemoteException {
        System.out.println("Topic: " + message.getTopic());
        System.out.println("Content: " + message.getContent());
        System.out.println();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override 
    public synchronized 
    void newPublisher(Publisher publisher) throws RemoteException {
        
        // Checking self-subscription is vital due to thread synchronization. 
        // RemoteObject does have appropriate equality semantics.
        //
        // CAVEAT: Docs say explicitly that RemoteObjects have appropriate
        // equality semantics. Well, they don't. Not really, that is.
        // Comparing stubs works fine, but comparing actual object to stub
        // doesn't. In order to achieve desired effect a stub needs to be
        // created from the object. RemoteObject has a toStub method, which
        // does just that.

        if (! publisher.equals(toStub(this))) {
            System.out.println("New relevant publisher");
            try {
                publisher.subscribe(this);
                publishers.add(publisher);
            } catch (RemoteException e) {
                System.err.println("Subscription error: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }
    
    
    /**
     * Main loop of a program - accepts and processes user input, after
     * encountering EOF gracefully halts.
     */
    public void inputLoop() throws IOException {
        String line = null;
        BufferedReader stdin = new BufferedReader(
                new InputStreamReader(System.in));
        while ((line = stdin.readLine()) != null) {
            try {
                interpretLine(line);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        finish();
    }


    private synchronized void interpretLine(String line) {
        Message message = new Message(publishedTopic, line);

        // Try everyone, ignore the errors 
        for (Subscriber s : subscribers) {
            try {
                s.notifyMessage(message);
            } catch (RemoteException e) {
                System.err.println("Error while sending: " + e.getMessage());
            }
        }
    }
    
    
    /**
     * Informs everyone who might be interested that the object's lifetime
     * is about to end.
     */
    private synchronized void finish() throws RemoteException {
        // We should try to unsubscribe from everyone, ignoring unsuccessfull
        // attempts - as it is now, errors are expected due to race conditions 
        try {
            server.unregisterPublisher(this);
        } catch (RemoteException e) {
            System.err.println("Error while halting (server): " + 
                    e.getMessage());
        }
        try {
            server.unregisterSubscriber(this);
        } catch (RemoteException e) {
            System.err.println("Error while halting (server): " + 
                    e.getMessage());
        }
        for (Publisher p : publishers) {
            try {
                p.unsubscribe(this);
            } catch (RemoteException e) {
                System.err.println("Error while halting (publisher): " + 
                        e.getMessage());
            }
        }
        for (Subscriber s : subscribers) {
            try {
                s.notifyUnsubscribed(this);
            } catch (RemoteException e) {
                System.err.println("Error while halting (subscriber): " + 
                        e.getMessage());
            }
        }
        // Unexporting is necessary to keep RMI thread from preventing
        // program from finishing execution
        UnicastRemoteObject.unexportObject(this, false);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized 
    void notifyUnsubscribed(Publisher publisher) throws RemoteException {
        System.out.println("Unsubscribed");
        publishers.remove(publisher);
    }

}
