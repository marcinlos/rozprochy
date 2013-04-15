package rozprochy.rok2012.lab1.zad4.connection;


public interface DatagramHandler<T> {

    void handle(byte type, T datagram);
    
}
