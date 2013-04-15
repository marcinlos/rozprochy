package rozprochy.lab3.common;

import org.omg.CORBA.UserException;

/**
 * Generic CORBA exception, designed as a wrapper for exceptions thrown by 
 * various CORBA components, hence the restricted exception types.
 */
public class CORBAException extends Exception {

    public CORBAException(String message, UserException cause) {
        super(message, cause);
    }

    public CORBAException(UserException cause) {
        super(cause);
    }
    
    @Override
    public UserException getCause() {
        return (UserException) super.getCause();
    }
    
}
