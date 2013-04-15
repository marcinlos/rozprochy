package rozprochy.rok2012.lab1.zad4.connection;

import java.nio.ByteBuffer;

public interface Parser<T> {

    T parse(byte type, ByteBuffer buffer);
    
}
