package bg.sofia.uni.fmi.mjt.cryptowallet.exceptions;

public class CryptoCurrencyNotInWalletException extends Exception {
    public CryptoCurrencyNotInWalletException(String message) {
        super(message);
    }
}
