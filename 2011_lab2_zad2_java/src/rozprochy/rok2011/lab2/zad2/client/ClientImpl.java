package rozprochy.rok2011.lab2.zad2.client;

import java.rmi.RemoteException;
import java.util.Collection;

import rozprochy.rok2011.lab2.zad2.common.Client;
import rozprochy.rok2011.lab2.zad2.common.Message;
import rozprochy.rok2011.lab2.zad2.common.Publisher;
import rozprochy.rok2011.lab2.zad2.common.Server;
import rozprochy.rok2011.lab2.zad2.common.Subscriber;


public class ClientImpl implements Client {

    private Server server;
    private String published;
    private Collection<String> subscribed;
    
    
    public ClientImpl(Server server, String published, 
            Collection<String> subscribed) {
        this.server = server;
        this.published = published;
        this.subscribed = subscribed;
    }
    
    
    @Override
    public void subscribe(Subscriber client) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unsubscribe(Subscriber client) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMessage(Message message) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void newPublisher(Publisher publisher) throws RemoteException {
        // TODO Auto-generated method stub

    }

}
