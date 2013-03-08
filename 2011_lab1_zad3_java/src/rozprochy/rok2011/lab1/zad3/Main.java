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
            CLIChatClient client = null;
            try {
                client = new CLIChatClient(login);
                int port = Integer.parseInt(args[1]);
                InetAddress group = InetAddress.getByName(ip);
                connection = new UDPConnection(client, group, port);
                client.setConnection(connection);
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
                client.inputLoop();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        
    }

}
