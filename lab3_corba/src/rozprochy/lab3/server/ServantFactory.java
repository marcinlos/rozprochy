package rozprochy.lab3.server;

import org.omg.PortableServer.Servant;

public interface ServantFactory {
    
    Servant create(String name);

}
