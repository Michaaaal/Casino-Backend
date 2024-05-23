package michal.malek.accounting.exceptions;

public class UserAccountNotFoundException extends RuntimeException{
    public UserAccountNotFoundException() {
    }

    public UserAccountNotFoundException(String message) {
        super(message);
    }

    public UserAccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAccountNotFoundException(Throwable cause) {
        super(cause);
    }
}
