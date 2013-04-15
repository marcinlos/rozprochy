package rozprochy.lab2.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import rozprochy.lab2.common.GameServer;
import rozprochy.lab2.common.Session;
import rozprochy.lab2.common.exceptions.LoginException;


public class ClientMain {

    public static void main(String[] args) {
        Config config = new Config(args);
        try {
            int port = config.getPort();
            String host = config.getHost();
            String nick = config.getLogin();
            Registry registry = LocateRegistry.getRegistry(host, port);
            GameServer server = (GameServer) registry.lookup(GameServer.NAME);
            System.out.println("Found server...");
            Session session = server.login(nick);
            Client client = new Client(server, session);
            client.run();
        } catch (RemoteException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (NotBoundException e) {
            System.err.println("Name `" + GameServer.NAME + "' is not registered");
        } catch (LoginException e) {
            System.err.println(e.getMessage());
        }
    }
    
}
