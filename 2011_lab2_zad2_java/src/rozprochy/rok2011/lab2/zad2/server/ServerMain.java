package rozprochy.rok2011.lab2.zad2.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import rozprochy.rok2011.lab2.zad2.common.Server;


public class ServerMain {
    
    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String address = "localhost";
            if (args.length != 0) {
                address = args[0];
            }
            String fullName = String.format("//%s/%s", address, Server.NAME);
            Server server = new ServerImpl();
            
            // GOTCHA !!!
            // Turns out passing the port argument is EXTREMELY important.
            // For some reason, without the port, exportObject returns a stub,
            // which it cannot find 
            // (mysterious "Cannot find class ServerImpl_Stub")
            Server stub = (Server) UnicastRemoteObject.exportObject(server, 0);
            Naming.rebind(fullName, stub);
            
            //Registry registry = LocateRegistry.getRegistry();
            //registry.rebind(Server.NAME, stub);
            
        } catch (RemoteException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace(System.err);
        } catch (MalformedURLException e) {
            System.err.println("Invalid RMI register address");
        }
    }
    
}
