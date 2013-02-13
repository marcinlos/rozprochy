package rozprochy.rok2011.lab2.zad2.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import rozprochy.rok2011.lab2.zad2.common.Publisher;
import rozprochy.rok2011.lab2.zad2.common.Server;
import rozprochy.rok2011.lab2.zad2.common.Subscriber;

/**
 * Implementation of a {@link Server} interface.
 */
public class ServerImpl implements Server {
    
    private static final Logger logger = Logger.getLogger("Server");
    
    /** Mapping Topic -> Publishers */
    private Map<String, List<Publisher>> publishers = 
            new HashMap<String, List<Publisher>>();

    /** Mapping Topic -> Subscribers */
    private Map<String, List<Subscriber>> subscribers = 
            new HashMap<String, List<Subscriber>>();

    
    /**
     * Creates a collection of all the publishers publishing on one of the 
     * topics mentioned by the passed iterator.
     */
    private Collection<Publisher> findPublishers(Iterable<String> topics) {
        List<Publisher> pub = new ArrayList<Publisher>();
        for (String topic : topics) {
            Collection<Publisher> onTopic = publishers.get(topic);
            if (onTopic != null) {
                pub.addAll(onTopic);
            }
        }
        return pub;
    }
    
    
    private void logSubscriber(Subscriber subscriber, Iterable<String> topics) {
        StringBuilder sb = new StringBuilder("New subscriber: ");
        for (String topic : topics) {
            sb.append(topic).append(' ');
        }
        logger.info(sb.toString());
    }
    
    
    /**
     * Adds a subscriber to all the lists of subscribers for topics 
     * in {@code topics}
     */
    private void addSubscriber(Subscriber subscriber, Iterable<String> topics) {
        for (String topic : topics) {
            List<Subscriber> list = subscribers.get(topic);
            if (list == null) {
                list = new ArrayList<Subscriber>();
                subscribers.put(topic, list);
            }
            list.add(subscriber);
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Collection<Publisher> registerSubscriber(
            Subscriber subscriber, Collection<String> topics) 
            throws RemoteException {
        logSubscriber(subscriber, topics);
        addSubscriber(subscriber, topics);
        return findPublishers(topics);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized
    void unregisterSubscriber(Subscriber subscriber)
            throws RemoteException {
        for (List<Subscriber> onTopic : subscribers.values()) {
            onTopic.remove(subscriber);
        }
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void registerPublisher(Publisher publisher, String topic)
            throws RemoteException {
        
        logger.info("New publisher: " + topic);
        addPublisher(publisher, topic);
        
        // Inform clients interested in this topic
        Iterable<Subscriber> interested = subscribers.get(topic);
        if (interested != null) {
            for (Subscriber subscriber : interested) {
                try {
                    subscriber.newPublisher(publisher);
                } catch (RemoteException e) {
                    System.err.println("Error while informing subscriber " + 
                            "about new publisher: " + e.getMessage());
                }
            }
        }
    }
    
    
    /**
     * Adds publisher to internal map
     */
    private void addPublisher(Publisher publisher, String topic) {
        List<Publisher> onTopic = publishers.get(topic);
        if (onTopic == null) {
            onTopic = new ArrayList<Publisher>();
            publishers.put(topic, onTopic);
        }
        onTopic.add(publisher);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized 
    void unregisterPublisher(Publisher publisher) throws RemoteException {
        logger.info("Publisher unregistered");
        for (List<Publisher> onTopic : publishers.values()) {
            if (onTopic.remove(publisher)) {
                break;
            }
        }
    }

}
