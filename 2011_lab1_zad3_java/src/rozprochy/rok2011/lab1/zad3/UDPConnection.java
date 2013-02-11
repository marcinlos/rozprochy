package rozprochy.rok2011.lab1.zad3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPConnection implements Connection {
    
    private static final int MAX_DATAGRAM = 1500;
    
    private ChatClient client;
    private MulticastSocket socket;
    private InetAddress address;
    private int port;

    
    public UDPConnection(ChatClient client, InetAddress address, int port) 
            throws IOException {
        
        System.out.println("Address: " + address.getHostAddress());
        
        if (! address.isMulticastAddress()) {
            String host = address.getHostAddress();
            String message = host + " is not a multicast address";
            throw new IllegalArgumentException(message);
        }
        this.address = address;
        this.port = port;
        this.client = client;
        
        createSocket();
    }
    
    
    @Override 
    public void run() throws IOException {
        Runnable input = new SocketListener();
        Thread listener = new Thread(input);
        listener.start();
    }
    
    
    private void createSocket() throws IOException {
        try {
            socket = new MulticastSocket(port);
            socket.joinGroup(address);
            
            // GOTCHA: the argument means whether or not to DISABLE
            // multicast loopback. It must be enabled if the application
            // is to work locally.
            socket.setLoopbackMode(false);
            // Doc says it's but a hint, so we check it explicitly
            if (socket.getLoopbackMode()) {
                System.err.println("Warning: couldn't set loopback mode, " + 
                        "application will not work locally\n");
            }
            // Not realy sure about this, seems to be a good idea
            socket.setReuseAddress(true);
        } catch (IOException e) {
            throw new IOException("Failed to create a multicast socket", e);
        }
    }
    

    @Override
    public void send(ChatDatagram data) throws IOException {
        byte[] buf = data.marshall();
        DatagramPacket p = new DatagramPacket(buf, buf.length, address, port);
        socket.send(p);
    }
    
    /**
     * Thread listening to incoming datagrams, and forwarding them to chat
     * client.
     */
    private class SocketListener implements Runnable {
        
        private byte[] buffer = new byte[MAX_DATAGRAM]; 
        private DatagramPacket datagram = 
                new DatagramPacket(buffer, buffer.length);

        @Override
        public void run() {
            try {
                while (true) {
                    receiveDatagram();
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        
        private void receiveDatagram() throws IOException {
            socket.receive(datagram);
            int offset = datagram.getOffset();
            int length = datagram.getLength();
            ChatDatagram data = ChatDatagram.unmarshall(buffer, offset, length);
            client.gotDatagram(data);
        }
        
    }

}
