package rozprochy.lab2.common.exceptions;

public class LoginException extends GameException {

    public LoginException() {
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(Throwable cause) {
        super(cause);
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

}
