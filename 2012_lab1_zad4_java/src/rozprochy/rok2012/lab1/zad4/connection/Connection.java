package rozprochy.rok2012.lab1.zad4.connection;

import java.io.IOException;

public interface Connection {
    
    void run() throws IOException;

    void send(byte type, Datagram data);
    
    <T> void addHandler(PacketFilter filter, Parser<T> parser, 
            DatagramHandler<? super T> handler);
    
    void close();
}
