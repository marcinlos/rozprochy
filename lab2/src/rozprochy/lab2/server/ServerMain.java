package rozprochy.lab2.server;

import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;

import rozprochy.lab2.common.GameServer;


public class ServerMain {
    
    public static void main(String[] args) {
        Config config = new Config(args);
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        String host = config.getHost();
        int port = config.getPort();
        GameServerImpl server = null;
        Registry registry = null;
        try {
            server = new GameServerImpl();
            registry = LocateRegistry.getRegistry(host, port);
            registry.rebind(GameServer.NAME, server);
            while (System.console().readLine() != null) { }
        } catch (RemoteException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace(System.err);
        } catch (AccessControlException e) {
            System.err.println(e.getMessage());
        } finally {
            if (server != null) {
                try {
                    server.shutdown();
                    UnicastRemoteObject.unexportObject(server, true);
                    System.out.println("Server unexported");
                    if (registry != null) {
                        registry.unbind(GameServer.NAME);
                    }
                } catch (NotBoundException e) {
                    System.err.println("Name was not bound");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
