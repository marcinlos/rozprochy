package rozprochy.lab2.common.exceptions;

public class NoSuchGameException extends JoinException {

    public NoSuchGameException() {
    }

    public NoSuchGameException(String message) {
        super(message);
    }

    public NoSuchGameException(Throwable cause) {
        super(cause);
    }

    public NoSuchGameException(String message, Throwable cause) {
        super(message, cause);
    }

}
