package michal.malek.auth.exceptions;

public class UserDontExistException extends RuntimeException{
    public UserDontExistException() {
    }

    public UserDontExistException(String message) {
        super(message);
    }

    public UserDontExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDontExistException(Throwable cause) {
        super(cause);
    }
}
