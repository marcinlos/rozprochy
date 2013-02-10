package rozprochy.rok2011.lab1.zad2;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Server accepting file from the client, writing it to a disc.
 */
public class Server {
    
    private static final Logger logger = 
            Logger.getLogger(Server.class.getName());
    
    /** Default port, used when it is not specified by the user */
    public static final int DEFAULT_PORT = 5000;
    
    private int port;
    private ServerSocket socket;
    private File directory;
    
    
    public static Server fromArguments(String[] args) {
        int port = DEFAULT_PORT;
        File dir = new File(".");
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number `" + args[0] + "'");
                printUsage();
                throw e;
            }
            if (args.length == 2) {
                dir = new File(args[1]);
            } else {
                System.err.println("Too much arguments");
                printUsage();        
            }
        }
        return new Server(dir, port);
    }

    private static void printUsage() {
        System.err.println("Usage: server <port> <directory>");
    }

    
    public Server(File directory, int port) {
        this.port = port;
        this.directory = directory;
    }
    
    /*
     * Utility method formatting socket's remote endpoint's address as ip:port
     */
    private static String formatAddress(Socket client) {
        InetAddress remote = client.getInetAddress();
        String ip = remote.getHostAddress();
        int port = client.getPort();
        return String.format("%s:%d", ip, port);
    }
    
    /**
     * Main loop, accepting connections and spawning threads to server
     * clients. 
     */
    public void run() throws IOException {
        try {
            socket = new ServerSocket(port);
            logger.info("Socket created");
            while (true) {
                Socket client = socket.accept();
                handleConnection(client);
            }
        } catch (IOException e) {
            logger.severe("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
    
    /**
     * Creates another thread to serve client represented by the passed socket
     */
    private void handleConnection(Socket client) {
        String address = formatAddress(client);
        logger.info("Accepted connection from " + address);
        ClientHandler handler = new ClientHandler(client, directory);
        Thread worker = new Thread(handler);
        worker.start();
    }
}
