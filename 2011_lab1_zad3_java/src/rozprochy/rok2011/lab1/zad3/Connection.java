package rozprochy.rok2011.lab1.zad3;

import java.io.IOException;

public interface Connection {

    void send(ChatDatagram data) throws IOException;
    
}
