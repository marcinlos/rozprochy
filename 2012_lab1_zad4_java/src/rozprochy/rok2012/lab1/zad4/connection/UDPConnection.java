package rozprochy.rok2012.lab1.zad4.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class UDPConnection implements Connection {
    
    private static final int MAX_DATAGRAM = 1500;
    
    private MulticastSocket socket;
    private InetAddress multicast;
    private int port;
    
    private volatile boolean closed = false;
    
    private Dispatcher dispatcher = new Dispatcher();

    
    public UDPConnection(InetAddress address, int port) 
            throws IOException {
        
        System.out.println("Address: " + address.getHostAddress());
        
        if (! address.isMulticastAddress()) {
            String host = address.getHostAddress();
            String message = host + " is not a multicast address";
            throw new IllegalArgumentException(message);
        }
        this.multicast = address;
        this.port = port;
        
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

            socket.joinGroup(multicast);
            
            // GOTCHA: the argument means whether or not to DISABLE
            // multicast loopback. It must be enabled if the application
            // is to work locally.
            socket.setLoopbackMode(false);
            // Doc says it's but a hint, so we check it explicitly
            if (socket.getLoopbackMode()) {
                System.err.println("Warning: couldn't set loopback mode, " + 
                        "application will not work locally\n");
            }
        } catch (IOException e) {
            throw new IOException("Failed to create a multicast socket", e);
        }
    }
    

    @Override
    public void send(byte type, Datagram data){
        try {
            ByteBuffer buf = ByteBuffer.allocate(MAX_DATAGRAM);
            buf.put(type);
            data.marshall(buf);
            buf.flip();
            byte[] array = buf.array();
            int len = buf.limit();
            DatagramPacket p = new DatagramPacket(array, len, multicast, port);
            socket.send(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            } catch (SocketException e) {
                if (! closed) {
                    System.err.println(e.getMessage());
                    e.printStackTrace(System.err);
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
            ByteBuffer bb = ByteBuffer.wrap(buffer, offset, length);
            try {
                dispatcher.dispatch(bb);
            } catch (NoHandlerException e) {
                System.err.println("Unknown packet type " + e.getType());
            }
        }
        
    }

    @Override
    public void close() {
        closed = true;
        socket.close();
    }


    @Override
    public <T> void addHandler(PacketFilter filter, Parser<T> parser,
            DatagramHandler<? super T> handler) {
        dispatcher.addHandler(filter, parser, handler);
    }

}
