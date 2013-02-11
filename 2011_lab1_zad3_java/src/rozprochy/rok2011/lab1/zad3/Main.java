package rozprochy.rok2011.lab1.zad3;

import java.net.InetAddress;

public class Main {

    private static void printUsage() {
        System.err.println("Usage: client group_ip port login");
    }
    
    public static void main(String[] args) {
        
        if (args.length < 3) {
            printUsage();
        } else {
            Connection connection = null;
            String ip = args[0];
            String login = args[2];
            try {
                int port = Integer.parseInt(args[1]);
                InetAddress group = InetAddress.getByName(ip);
                connection = new UDPConnection(group, port);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                printUsage();
                return;
            }
            CLIChatClient client = new CLIChatClient(connection, login);
            try {
                client.inputLoop();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
    }

}
