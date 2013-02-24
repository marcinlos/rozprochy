package rozprochy.rok2011.lab3.zad1.server;

/**
 * Base class for all the laboratory-related exceptions
 */
public class LabException extends Exception {

    public LabException() {
        
    }

    public LabException(String message) {
        super(message);
    }

    public LabException(Throwable cause) {
        super(cause);
    }

    public LabException(String message, Throwable cause) {
        super(message, cause);
    }

}
