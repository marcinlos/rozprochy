package rozprochy.rok2011.lab2.zad1.client.rmi;

import java.io.IOException;
import java.io.Serializable;

import rozprochy.rok2011.lab2.zad1.common.Task;


/**
 * Task that always fails - to check error reporting 
 */
public class Failure implements Task<Void>, Serializable {

    @Override
    public Void execute() {
        throw new RuntimeException("Some error has occured", 
                new IOException("Somethink is broken"));
    }
    
    @Override
    public String toString() {
        return "Fail unconditionally";
    }

    
}
