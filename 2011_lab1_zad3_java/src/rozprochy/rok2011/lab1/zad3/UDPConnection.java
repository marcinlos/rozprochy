package rozprochy.rok2011.lab1.zad3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPConnection implements Connection {
    
    private static final int MAX_DATAGRAM = 1500;
    
    
    private MulticastSocket socket;
    private InetAddress address;
    private int port;

    
    public UDPConnection(InetAddress address, int port) throws IOException {
        if (address.isMulticastAddress()) {
            String host = address.getHostAddress();
            String message = host + " is not a multicast address";
            throw new IllegalArgumentException(message);
        }
        this.address = address;
        this.port = port;
        
        createSocket();
    }
    
    
    public void run() throws IOException {
        Runnable input = new SocketListener();
        Thread listener = new Thread(input);
        listener.start();
    }
    
    
    private void createSocket() throws IOException {
        try {
            socket = new MulticastSocket(port);
            socket.joinGroup(address);
        } catch (IOException e) {
            throw new IOException("Failed to create a multicast socket", e);
        }
    }
    

    @Override
    public void send(ChatDatagram data) throws IOException {
        byte[] buf = data.marshall();
        DatagramPacket datagram = new DatagramPacket(buf, 0, 
                buf.length, address, port);
        socket.send(datagram);
    }
    
    
    private class SocketListener implements Runnable {
        
        private byte[] buffer = new byte[MAX_DATAGRAM]; 
        private DatagramPacket datagram = 
                new DatagramPacket(buffer, buffer.length);

        @Override
        public void run() {
            try {
                receiveDatagram();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        
        private void receiveDatagram() throws IOException {
            socket.receive(datagram);
        }
        
    }

}
