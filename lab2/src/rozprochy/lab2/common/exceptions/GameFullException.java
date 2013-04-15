package rozprochy.lab2.common.exceptions;

public class GameFullException extends JoinException {

    public GameFullException() {
    }

    public GameFullException(String message) {
        super(message);
    }

    public GameFullException(Throwable cause) {
        super(cause);
    }

    public GameFullException(String message, Throwable cause) {
        super(message, cause);
    }

}
