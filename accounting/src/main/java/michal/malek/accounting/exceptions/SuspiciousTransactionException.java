package michal.malek.accounting.exceptions;

public class SuspiciousTransactionException extends RuntimeException{
    public SuspiciousTransactionException() {
        super();
    }

    public SuspiciousTransactionException(String message) {
        super(message);
    }

    public SuspiciousTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SuspiciousTransactionException(Throwable cause) {
        super(cause);
    }
}
