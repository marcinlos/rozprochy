package rozprochy.lab2.common.exceptions;

public class MoveException extends Exception {

    public MoveException() {
    }

    public MoveException(String message) {
        super(message);
    }

    public MoveException(Throwable cause) {
        super(cause);
    }

    public MoveException(String message, Throwable cause) {
        super(message, cause);
    }

}
