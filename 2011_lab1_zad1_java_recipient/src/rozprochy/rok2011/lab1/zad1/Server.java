package rozprochy.rok2011.lab1.zad1;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;


public class Server {
    
    static final Logger logger = Logger.getLogger(Server.class.getName());

    /** Port used when port is not specified by the user */
    private static final int DEFAULT_PORT = 5000;
    
    private int port;
    private ServerSocket socket;
    
    private Service service;
    
    /**
     * Factory method creating server from command line arguments.
     * 
     * @param args command line arguments
     * @param service integral value transformer
     */
    public static Server fromArguments(Service service, String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                printUsage();
                throw e;
            }
        }
        logger.info("Port " + port + " choosen");
        return new Server(service, port);
    }
    
    private static void printUsage() {
        System.err.println("Usage: server <port>");
    }


    public Server(Service service, int port) {
        this.service = service;
        this.port = port;
    }

    /**
     * Method responsible for creation of socket and accepting incoming
     * connections.
     */
    public void run() throws IOException {
        try  {
            socket = new ServerSocket(port);
            while (true) {
                acceptConnection();
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
            throw e;
        } finally {
            if (socket != null) {
                socket.close();
            } 
        }
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
    
    /*
     * Handles details of single connection
     */
    private void acceptConnection() throws IOException {
        Socket client = null;
        try {
            client = socket.accept();
            // Log the other endpoint of a connection
            String address = formatAddress(client);
            logger.info("Accepted connection from " + address);
            
            serveClient(client);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
    
    /*
     * Method doing the actual job of talking to client
     */
    private void serveClient(Socket client) throws IOException {
        
        DataInput input = new DataInputStream(client.getInputStream());
        DataOutput output = new DataOutputStream(client.getOutputStream());
        
        byte type = input.readByte();

        if (type == 'b') {
            byte v = input.readByte();
            logger.info("Received byte: " + v);
            byte p = service.process(v);
            output.writeByte(p);
        }
        else if (type == 's') {
            short v = input.readShort();
            logger.info("Received short: " + v);
            short p = service.process(v);
            output.writeShort(p);
        }
        else if (type == 'w') {
            int v = input.readInt();
            logger.info("Received int: " + v);
            int p = service.process(v);
            output.writeInt(p);
        }
        else if (type == 'd') {
            long v = input.readLong();
            logger.info("Received long: " + v);
            long p = service.process(v);
            output.writeLong(p);
        }
    }

}
