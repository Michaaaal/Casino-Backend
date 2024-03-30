package michal.malek.auth.exceptions;

public class UserExistingWithLogin extends RuntimeException{
    public UserExistingWithLogin() {
    }

    public UserExistingWithLogin(String message) {
        super(message);
    }

    public UserExistingWithLogin(String message, Throwable cause) {
        super(message, cause);
    }

    public UserExistingWithLogin(Throwable cause) {
        super(cause);
    }
}
