package bg.sofia.uni.fmi.mjt.cryptowallet.exceptions;

public class NotEnoughMoneyInWalletException extends Exception {
    public NotEnoughMoneyInWalletException(String message) {
        super(message);
    }
}
