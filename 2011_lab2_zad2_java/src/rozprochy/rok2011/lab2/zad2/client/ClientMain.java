package rozprochy.rok2011.lab2.zad2.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rozprochy.rok2011.lab2.zad2.common.Client;
import rozprochy.rok2011.lab2.zad2.common.Server;

public class ClientMain {

    private static void printUsage() {
        System.err.println("Usage: " + 
                "client server published_topic <subscribed_topics> ...");
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            printUsage();
            return;
        }
        try {
            Registry registry = LocateRegistry.getRegistry(args[0]);
            Server server = (Server) registry.lookup(Server.NAME);
            
            System.out.println("Found server...");
            
            String published = args[1]; 
            List<String> subscribed = new ArrayList<String>(
                    Arrays.asList(args)
                    .subList(2, args.length));
            
            Client client = new ClientImpl(server, published, subscribed);
            Client stub = (Client) UnicastRemoteObject.exportObject(client, 0);

            server.registerPublisher(stub, published);
            server.registerSubscriber(stub, subscribed);
            
        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        } catch (NotBoundException e) {
            System.err.println("Name `" + Server.NAME + "' is not registered");
        }
    }
    
}
