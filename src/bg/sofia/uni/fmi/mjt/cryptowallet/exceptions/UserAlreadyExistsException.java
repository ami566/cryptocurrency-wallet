package bg.sofia.uni.fmi.mjt.cryptowallet.exceptions;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
