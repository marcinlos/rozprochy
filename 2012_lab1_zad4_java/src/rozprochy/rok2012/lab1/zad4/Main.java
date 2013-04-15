package rozprochy.rok2012.lab1.zad4;

import java.net.InetAddress;

import rozprochy.rok2012.lab1.zad4.cli.CLI;
import rozprochy.rok2012.lab1.zad4.connection.AnyBitFilter;
import rozprochy.rok2012.lab1.zad4.connection.Connection;
import rozprochy.rok2012.lab1.zad4.connection.ExactFilter;
import rozprochy.rok2012.lab1.zad4.connection.PacketFilter;
import rozprochy.rok2012.lab1.zad4.connection.UDPConnection;


public class Main {

    private static void printUsage() {
        System.err.println("Usage: client group_ip port login");
    }
    
    public static void main(String[] args) {
        
        if (args.length < 3) {
            printUsage();
            return;
        } 
        Connection connection = null;
        String ip = args[0];
        String login = args[2];
        ChatClient client = null;
        ElectionFSM election = null;
        CLI cli = null;
        try {
            int port = Integer.parseInt(args[1]);
            InetAddress group = InetAddress.getByName(ip);
            connection = new UDPConnection(group, port);
            cli = new CLI();

            client = new ChatClient(login);
            client.setConnection(connection);
            PacketFilter filter = new ExactFilter(DatagramType.MESSAGE);
            connection.addHandler(filter, ChatClient.getParser(), client);
            cli.setDefaultHandler(client);
            
            // Probably unique identifier for the election algorhtm
            long h = login.hashCode();
            long id = (h << 32) ^ h ^ System.nanoTime();
            
            election = new ElectionFSM(id, login, connection);
            filter = new AnyBitFilter((byte) 4);
            connection.addHandler(filter, ElectionFSM.getParser(), election);
            cli.addHandler("/election", election);
            
        } catch (LoginTooLongException e) {
            System.err.println("Login too long");
            return;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            printUsage();
            return;
        }
        try {
            connection.run();
            cli.run();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            election.shutdown();
            connection.close();
        }
    }
}
