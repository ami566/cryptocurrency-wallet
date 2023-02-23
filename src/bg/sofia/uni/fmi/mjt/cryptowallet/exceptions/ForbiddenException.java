package bg.sofia.uni.fmi.mjt.cryptowallet.exceptions;

public class ForbiddenException extends HttpException {
    public ForbiddenException(String message) {
        super(message);
    }
}
