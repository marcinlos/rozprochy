package rozprochy.lab2.common.exceptions;

public class JoinException extends Exception {

    public JoinException() {
    }

    public JoinException(String message) {
        super(message);
    }

    public JoinException(Throwable cause) {
        super(cause);
    }

    public JoinException(String message, Throwable cause) {
        super(message, cause);
    }

}
