package bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction;

public abstract class CryptoTransaction extends Transaction {
    private double moneyInDollars;
    private double moneyInCrypto;
    private String cryptoAsset;
    private double priceOfAsset;

    protected CryptoTransaction(double dollars, double crypto, String asset, double price) {
        moneyInDollars = dollars;
        moneyInCrypto = crypto;
        cryptoAsset = asset;
        priceOfAsset = price;
    }

    public double getMoneyInDollars() {
        return moneyInDollars;
    }

    public double getMoneyInCrypto() {
        return moneyInCrypto;
    }

    public String getCryptoAsset() {
        return cryptoAsset;
    }

}
