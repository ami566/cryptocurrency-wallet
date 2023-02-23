package bg.sofia.uni.fmi.mjt.cryptowallet.exceptions;

public class BadRequestException extends HttpException {
    public BadRequestException(String message) {
        super(message);
    }
}
