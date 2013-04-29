package rozprochy.lab4.util;

import java.io.IOException;

public class IOExceptionWrapper extends RuntimeException {

    public IOExceptionWrapper(IOException cause) {
        super(cause);
    }

    public IOException wrapped() {
        return (IOException) getCause();
    }
    
    public static IOExceptionWrapper wrapIO(IOException e) {
        return new IOExceptionWrapper(e);
    }
    
}
