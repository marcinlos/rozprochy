package rozprochy.rok2012.lab1.zad4.connection;

import java.nio.ByteBuffer;

public class EmptyDatagram implements Datagram {
    
    public final static EmptyDatagram INSTANCE = new EmptyDatagram();

    @Override
    public void marshall(ByteBuffer buffer) {
        // do nothing
    }

}
