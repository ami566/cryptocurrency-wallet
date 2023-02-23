package bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction;

public class SoldCryptoTransaction extends CryptoTransaction {

    public SoldCryptoTransaction(double dollars, double crypto, String asset, double price) {
        super(dollars, crypto, asset, price);
    }

    @Override
    public String transactionString() {
        return "Sold " + getMoneyInCrypto() + " " + getCryptoAsset() + " for " + getMoneyInDollars() + " USD.";
    }
}
