package bg.sofia.uni.fmi.mjt.cryptowallet.exceptions;

public class TooManyRequestsException extends HttpException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
