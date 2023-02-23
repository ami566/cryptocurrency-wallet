package bg.sofia.uni.fmi.mjt.cryptowallet.exceptions;

public class UnauthorizedException extends HttpException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

