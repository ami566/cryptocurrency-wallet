package bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction;

public class BoughtCryptoTransaction extends CryptoTransaction {

    public BoughtCryptoTransaction(double dollars, double crypto, String asset, double price) {
        super(dollars, crypto, asset, price);
    }

    @Override
    public String transactionString() {
        return "Bought " + getMoneyInCrypto() + " " + getCryptoAsset() + " for " + getMoneyInDollars() + " USD.";
    }
}
